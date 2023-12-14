(ns status-im2.contexts.wallet.collectible.tabs.about.view
  (:require [quo.core :as quo]
            [quo.theme]
            [react-native.core :as rn] 
            [status-im2.contexts.wallet.collectible.tabs.about.style :as style]))

(def link-cards
  [{:title "BAYC"
    :icon  :social/link
    :address "boredapeyachtclub"
    :customization-color :link
    :on-press #(js/alert "pressed")}
   {:title "Twitter"
    :icon  :social/twitter
    :address "@BoredApeYC"
    :customization-color :twitter
    :on-press #(js/alert "pressed")}
   {:title "Opensea"
    :icon  :social/opensea
    :address "Bored Ape Yacht Club"
    :customization-color :opensea
    :on-press #(js/alert "pressed")}])

(defn- view-internal
  []
  [:<>
   [rn/view {:style style/title}
    [quo/text
     {:size   :heading-2
      :weight :semi-bold}
     "Bored Ape Yacht Club"]]
   [rn/view {:style style/description}
    [quo/text
     {:size :paragraph-2}
     "The Bored Ape Yacht Club is a collection of 10,000 unique Bored Ape NFTs— unique digital collectibles living on the Ethereum blockchain. Your Bored Ape doubles as your Yacht Club membership card, and grants access to members-only benefits, the first of which is access to THE BATHROOM, a collaborative graffiti board. Future areas and perks can be unlocked by the community through roadmap activation. Visit www.BoredApeYachtClub.com for more details."]]
   [quo/section-label {:container-style style/section-label
                        :section         "On the web"}]
   [rn/view {:style style/link-cards-container}
    (for [item link-cards]
      (quo/link-card (assoc item :container-style style/link-card)))]])

(def view (quo.theme/with-theme view-internal))
