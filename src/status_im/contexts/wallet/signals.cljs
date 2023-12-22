(ns status-im.contexts.wallet.signals
  (:require
    [taoensso.timbre :as log]
    [utils.re-frame :as rf]))

(rf/reg-event-fx
 :wallet/signal-fired
 (fn [_ {:keys [type blockNumber accounts] :as event}]
   (log/debug "[wallet-subs] new-wallet-event"
              "event-type"  type
              "blockNumber" blockNumber
              "accounts"    accounts)
   (case type
     "wallet-owned-collectibles-filtering-done" {:fx [[:dispatch
                                                       [:wallet/owned-collectibles-filtering-done
                                                        event]]]}
     "wallet-get-collectibles-details-done"     {:fx [[:dispatch
                                                       [:wallet/get-collectible-details-done
                                                        event]]]}
     (log/debug ::unknown-wallet-event :type type :event event))))
