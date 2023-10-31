(ns quo.components.colors.color-picker.view
  (:require
    [quo.components.colors.color.view :as color]
    [react-native.core :as rn]
    [reagent.core :as reagent]))

(def color-list
  [:blue :yellow :purple :turquoise :magenta :sky :orange :army :flamingo :camel :copper])

(defn- on-change-handler
  [selected color-name on-change]
  (reset! selected color-name)
  (when on-change (on-change color-name)))

(defn view
  "Options
   - `default-selected` Default selected color name.
   - `on-change` Callback called when a color is selected `(fn [color-name])`.
   - `blur?` Boolean to enable blur background support.}"
  [{:keys [default-selected window-width]}]
  (let [selected (reagent/atom default-selected)]
    (fn [{:keys [blur? on-change feng-shui? container-style]}]
      )))
