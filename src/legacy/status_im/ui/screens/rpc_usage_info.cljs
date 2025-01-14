(ns legacy.status-im.ui.screens.rpc-usage-info
  (:require
    [clojure.string :as string]
    [legacy.status-im.ui.components.core :as legacy.status-im.ui.components.core]
    [legacy.status-im.ui.components.react :as react]
    [legacy.status-im.ui.components.typography :as typography]
    [legacy.status-im.utils.utils :as utils]
    [re-frame.core :as re-frame]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.common.json-rpc.events :as json-rpc]
    [taoensso.timbre :as log]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(re-frame/reg-sub :rpc-usage/raw-data (fn [db] (get db :rpc-usage/data)))
(re-frame/reg-sub :rpc-usage/filter (fn [db] (get db :rpc-usage/filter)))

(re-frame/reg-sub
 :rpc-usage/data
 :<- [:rpc-usage/raw-data]
 :<- [:rpc-usage/filter]
 (fn [[{total :total rpc-methods :methods} method-filter]]
   (let [data
         (->> rpc-methods
              (map (fn [[k v]]
                     [(name k) v]))
              (filter (fn [[k]]
                        (string/includes? k method-filter)))
              (sort-by second >))
         filtered-total (reduce + (map second data))]
     {:stats          data
      :filtered-total filtered-total
      :total          (or total 0)})))

(re-frame/reg-fx
 ::get-stats
 (fn []
   (json-rpc/call
    {:method     "rpcstats_getStats"
     :params     []
     :on-success #(re-frame/dispatch [::handle-stats %])})))

(re-frame/reg-fx
 ::reset
 (fn []
   (json-rpc/call
    {:method     "rpcstats_reset"
     :params     []
     :on-success #(log/debug "rpcstats_reset success")})))

;; RPC refresh interval ID
(defonce rpc-refresh-interval (atom nil))

;; RPC usage refresh interval (ms)
(def rpc-usage-refresh-interval-ms 2000)

(rf/defn handle-stats
  {:events [::handle-stats]}
  [{:keys [db]} data]
  {:db (assoc db :rpc-usage/data data)})

(rf/defn get-stats
  {:events [::get-stats]}
  [{:keys [db]}]
  (let [method-filter (get db :rpc-usage/filter "eth_")]
    {:db         (assoc db :rpc-usage/filter method-filter)
     ::get-stats nil}))

(rf/defn reset
  {:events [::reset]}
  [{:keys [db]}]
  {:db     (dissoc db :rpc-usage/data)
   ::reset nil})

(rf/defn copy
  {:events [::copy]}
  [{:keys [db]}]
  {:db     (dissoc db :rpc-usage/data)
   ::reset nil})

(rf/defn set-filter
  {:events [::set-filter]}
  [{:keys [db]} method-filter]
  {:db (assoc db :rpc-usage/filter method-filter)})

(defn stats-table
  [{:keys [total filtered-total stats]}]
  rn/scroll-view
  {:style {:padding-horizontal 8}}
  rn/view
  {:style {:flex-direction  :row
           :justify-content :space-between
           :margin-bottom   2}}
  [legacy.status-im.ui.components.core/text {:style typography/font-semi-bold}
   (i18n/label :t/rpc-usage-total)]
  [legacy.status-im.ui.components.core/text {:style typography/font-semi-bold}
   (i18n/label :t/rpc-usage-filtered-total {:filtered-total filtered-total :total total})]
  (when (seq stats)
    (for [[k v] stats]
      ^{:key (str k v)}
      [:<>
       rn/view
       {:style {:flex-direction  :row
                :align-items     :center
                :margin-vertical 10}}
       [legacy.status-im.ui.components.core/text {:style {:flex 1}}
        k]
       [legacy.status-im.ui.components.core/text {:style {:margin-left 16}}
        v]
       [legacy.status-im.ui.components.core/separator]])))

(defn prepare-stats
  [{:keys [stats]}]
  (string/join
   "\n"
   (map (fn [[k v]]
          (str k " " v))
        stats)))

(defn usage-info-render
  []
  (let [stats          @(re-frame/subscribe [:rpc-usage/data])
        methods-filter @(re-frame/subscribe [:rpc-usage/filter])]
    [react/view
     {:flex              1
      :margin-horizontal 8}
     rn/view
     {:style {:flex-direction  :row
              :margin-vertical 8
              :justify-content :space-between}}
     [legacy.status-im.ui.components.core/button
      {:on-press            #(re-frame/dispatch [::reset])
       :accessibility-label :rpc-usage-reset}
      (i18n/label :t/rpc-usage-reset)]
     [legacy.status-im.ui.components.core/button
      {:on-press
       #(react/copy-to-clipboard (prepare-stats stats))
       :accessibility-label :rpc-usage-copy}
      (i18n/label :t/rpc-usage-copy)]
     [legacy.status-im.ui.components.core/text-input
      {:on-change-text  #(re-frame/dispatch [::set-filter %])
       :label           (i18n/label :t/rpc-usage-filter-methods)
       :placeholder     (i18n/label :t/rpc-usage-filter)
       :container-style {:margin-vertical 18}
       :before          {:icon  :main-icons/search
                         :style {:padding-horizontal 8}}
       :default-value   methods-filter
       :auto-capitalize :none
       :show-cancel     false
       :auto-focus      false}]
     [stats-table stats]]))

(defn usage-info
  []
  (reagent/create-class
   {:component-did-mount
    (fn []
      (reset! rpc-refresh-interval
        (utils/set-interval #(re-frame/dispatch [::get-stats]) rpc-usage-refresh-interval-ms)))

    :component-will-unmount (fn []
                              (utils/clear-interval @rpc-refresh-interval)
                              (reset! rpc-refresh-interval nil))
    :reagent-render usage-info-render}))
