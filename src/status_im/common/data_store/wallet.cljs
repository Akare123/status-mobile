(ns status-im.common.data-store.wallet
  (:require
    [camel-snake-kebab.core :as csk]
    [camel-snake-kebab.extras :as cske]
    [clojure.set :as set]
    [clojure.string :as string]
    [status-im.constants :as constants]
    [utils.money :as money]
    [utils.number :as utils.number]))

(defn chain-ids-string->set
  [ids-string]
  (into #{}
        (map utils.number/parse-int)
        (string/split ids-string constants/chain-id-separator)))

(defn chain-ids-set->string
  [ids]
  (string/join constants/chain-id-separator ids))

(defn add-keys-to-account
  [account]
  (assoc account :watch-only? (= (:type account) :watch)))

(defn rpc->account
  [account]
  (-> account
      (set/rename-keys {:prodPreferredChainIds :prod-preferred-chain-ids
                        :testPreferredChainIds :test-preferred-chain-ids
                        :createdAt             :created-at
                        :colorId               :color})
      (update :prod-preferred-chain-ids chain-ids-string->set)
      (update :test-preferred-chain-ids chain-ids-string->set)
      (update :type keyword)
      (update :color #(if (seq %) (keyword %) constants/account-default-customization-color))
      add-keys-to-account))

(defn rpc->accounts
  [accounts]
  (->> (filter #(not (:chat %)) accounts)
       (sort-by :position)
       (map rpc->account)))

(defn <-account
  [account]
  (-> account
      (set/rename-keys {:prod-preferred-chain-ids :prodPreferredChainIds
                        :test-preferred-chain-ids :testPreferredChainIds
                        :color                    :colorId})
      (update :prodPreferredChainIds chain-ids-set->string)
      (update :testPreferredChainIds chain-ids-set->string)
      (dissoc :watch-only?)))

(defn- rpc->balances-per-chain
  [token]
  (-> token
      (update :balances-per-chain
              update-vals
              #(-> %
                   (update :raw-balance money/bignumber)
                   (update :balance money/bignumber)))
      (update :balances-per-chain update-keys (comp utils.number/parse-int name))))

(defn rpc->tokens
  [tokens]
  (-> tokens
      (update-keys name)
      (update-vals #(cske/transform-keys csk/->kebab-case %))
      (update-vals #(mapv rpc->balances-per-chain %))))

(defn <-rpc
  [network]
  (-> network
      (set/rename-keys
       {:Prod                   :prod
        :Test                   :test
        :isTest                 :test?
        :tokenOverrides         :token-overrides
        :rpcUrl                 :rpc-url
        :chainColor             :chain-color
        :chainName              :chain-name
        :nativeCurrencyDecimals :native-currency-decimals
        :relatedChainId         :related-chain-id
        :shortName              :short-name
        :chainId                :chain-id
        :originalFallbackURL    :original-fallback-url
        :originalRpcUrl         :original-rpc-url
        :fallbackURL            :fallback-url
        :blockExplorerUrl       :block-explorer-url
        :nativeCurrencySymbol   :native-currency-symbol
        :nativeCurrencyName     :native-currency-symbol})))
