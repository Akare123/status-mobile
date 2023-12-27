(ns status-im.contexts.wallet.common.utils-test
  (:require [cljs.test :refer [deftest is testing]]
            [status-im.contexts.wallet.common.utils :as utils]
            [utils.money :as money]))

(deftest test-get-wallet-qr
  (testing "Test get-wallet-qr function"
    (let [wallet-multichain  {:wallet-type       :wallet-multichain
                              :selected-networks [:ethereum :optimism]
                              :address           "x000"}
          wallet-singlechain {:wallet-type       :wallet-singlechain
                              :selected-networks [:ethereum :optimism]
                              :address           "x000"}]

      (is (= (utils/get-wallet-qr wallet-multichain)
             "eth:opt:x000"))

      (is (= (utils/get-wallet-qr wallet-singlechain)
             "x000")))))

(deftest test-calculate-gas-fee
  (testing "Test calculate-gas-fee function with EIP-1559 enabled"
    (let [data-eip1559-enabled            {:GasAmount "23487"
                                           :GasFees   {:baseFee              "32.325296406"
                                                       :maxPriorityFeePerGas "0.011000001"
                                                       :eip1559Enabled       true}}
          expected-eip1559-enabled-result (money/bignumber 0.0007594826)]
      (is (money/equal-to (utils/calculate-gas-fee data-eip1559-enabled)
                          expected-eip1559-enabled-result)))

    (testing "Test calculate-gas-fee function with EIP-1559 disabled"
      (let [data-eip1559-disabled            {:GasAmount "23487"
                                              :GasFees   {:gasPrice       "32.375609968"
                                                          :eip1559Enabled false}}
            expected-eip1559-disabled-result (money/bignumber 0.000760406)]
        (is (money/equal-to (utils/calculate-gas-fee data-eip1559-disabled)
                            expected-eip1559-disabled-result))))))
