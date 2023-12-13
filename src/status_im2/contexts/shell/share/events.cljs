(ns status-im2.contexts.shell.share.events
  (:require
    [quo.foundations.colors :as colors]
    [status-im2.common.toasts.events :as toasts]
    [utils.re-frame :as rf]))

(rf/defn copy-text-and-show-toast
  {:events [:share/copy-text-and-show-toast]}
  [{:keys [db] :as cofx} {:keys [text-to-copy post-copy-message]}]
  (rf/merge cofx
            {:copy-to-clipboard text-to-copy}
            (toasts/upsert
             {:icon       :i/correct
              :id         :successful-copy-toast-message
              :icon-color colors/success-50
              :theme      :dark
              :text       post-copy-message})))
