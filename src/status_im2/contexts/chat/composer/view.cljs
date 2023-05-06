(ns status-im2.contexts.chat.composer.view
  (:require
    [quo2.foundations.colors :as colors]
    [react-native.core :as rn]
    [react-native.gesture :as gesture]
    [react-native.hooks :as hooks]
    [react-native.reanimated :as reanimated]
    [reagent.core :as reagent]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [status-im2.contexts.chat.composer.style :as style]
    [status-im2.contexts.chat.composer.images.view :as images]
    [status-im2.contexts.chat.composer.reply.view :as reply]
    [status-im2.contexts.chat.composer.edit.view :as edit]
    [status-im2.contexts.chat.composer.mentions.view :as mentions]
    [status-im2.contexts.chat.composer.utils :as utils]
    [status-im2.contexts.chat.composer.constants :as constants]
    [status-im2.contexts.chat.composer.actions.view :as actions]
    [status-im2.contexts.chat.composer.keyboard :as kb]
    [status-im2.contexts.chat.composer.sub-view :as sub-view]
    [status-im2.contexts.chat.composer.effects :as effects]
    [status-im2.contexts.chat.composer.gesture :as drag-gesture]
    [status-im2.contexts.chat.composer.handlers :as handler]
    [status-im2.contexts.chat.composer.gradients.view :as gradients]
    [status-im2.contexts.chat.composer.selection :as selection]))

(defn sheet-component
  [{:keys [insets window-height blur-height opacity background-y]} props state]
  (let [images                                   (rf/sub [:chats/sending-image])
        audio                                    (rf/sub [:chats/sending-audio])
        reply                                    (rf/sub [:chats/reply-message])
        edit                                     (rf/sub [:chats/edit-message])
        input-with-mentions                      (rf/sub [:chat/input-with-mentions])
        {:keys [input-text input-content-height]
         :as   chat-input}                       (rf/sub [:chats/current-chat-input])
        content-height                           (reagent/atom (or input-content-height
                                                                   constants/input-height))
        {:keys [keyboard-shown keyboard-height]} (hooks/use-keyboard)
        kb-height                                (kb/get-kb-height keyboard-height
                                                                   @(:kb-default-height state))
        max-height                               (utils/calc-max-height window-height
                                                                        kb-height
                                                                        insets
                                                                        (boolean (seq images))
                                                                        reply
                                                                        edit)
        lines                                    (utils/calc-lines @content-height)
        max-lines                                (utils/calc-lines max-height)
        initial-height                           (if (> lines 1)
                                                   constants/multiline-minimized-height
                                                   constants/input-height)
        animations                               {:gradient-opacity  (reanimated/use-shared-value 0)
                                                  :container-opacity (reanimated/use-shared-value
                                                                      (if (utils/empty-input?
                                                                           input-text
                                                                           images
                                                                           reply
                                                                           audio)
                                                                        0.7
                                                                        1))
                                                  :height            (reanimated/use-shared-value
                                                                      initial-height)
                                                  :saved-height      (reanimated/use-shared-value
                                                                      initial-height)
                                                  :last-height       (reanimated/use-shared-value
                                                                      (utils/bounded-val
                                                                       @content-height
                                                                       constants/input-height
                                                                       max-height))
                                                  :opacity           opacity
                                                  :background-y      background-y}
        dimensions                               {:content-height content-height
                                                  :max-height     max-height
                                                  :window-height  window-height
                                                  :lines          lines
                                                  :max-lines      max-lines}
        show-bottom-gradient?                    (utils/show-bottom-gradient? state dimensions)
        cursor-pos                               (utils/cursor-y-position-relative-to-container props
                                                                                                state)]
    (effects/initialize props
                        state
                        animations
                        dimensions
                        chat-input
                        keyboard-height
                        (boolean (seq images))
                        reply
                        edit
                        audio)
    (effects/setup-selection props)
    (effects/update-input-mention props state input-text)
    (effects/edit-mentions props state input-with-mentions)
    [:<>
     [mentions/view props state animations max-height cursor-pos]
     [gesture/gesture-detector
      {:gesture (drag-gesture/drag-gesture props state animations dimensions keyboard-shown)}
      [reanimated/view
       {:style     (style/sheet-container insets state animations)
        :on-layout #(handler/layout % state blur-height)}
       [sub-view/bar]
       [reply/view state]
       [edit/view state]
       [reanimated/touchable-opacity
        {:active-opacity      1
         :on-press            (when @(:input-ref props) #(.focus ^js @(:input-ref props)))
         :style               (style/input-container (:height animations) max-height)
         :accessibility-label :message-input-container}
        [rn/selectable-text-input
         {:ref        #(reset! (:selectable-input-ref props) %)
          :menu-items @(:menu-items state)
          :style      (style/input-view props state)}
         [rn/text-input
          {:ref                      #(reset! (:input-ref props) %)
           :default-value            @(:text-value state)
           :on-focus                 #(handler/focus props state animations dimensions)
           :on-blur                  #(handler/blur state animations dimensions images reply)
           :on-content-size-change   #(handler/content-size-change %
                                                                   state
                                                                   animations
                                                                   dimensions
                                                                   (or keyboard-shown edit))
           :on-scroll                #(handler/scroll % props state animations dimensions)
           :on-change-text           #(handler/change-text % props state)
           :on-selection-change      #(handler/selection-change % props state)
           :on-selection             #(selection/on-selection % props state)
           :max-height               max-height
           :max-font-size-multiplier 1
           :multiline                true
           :placeholder              (i18n/label :t/type-something)
           :placeholder-text-color   (colors/theme-colors colors/neutral-40 colors/neutral-50)
           :style                    (style/input-text)
           :max-length               constants/max-text-size
           :accessibility-label      :chat-message-input}]]
        [gradients/view props state animations show-bottom-gradient?]]
       [images/images-list]
       [actions/view props state animations window-height insets edit
        (boolean (seq images))]]]]))

(defn composer
  [insets]
  (let [window-height (rf/sub [:dimensions/window-height])
        opacity       (reanimated/use-shared-value 0)
        background-y  (reanimated/use-shared-value (- window-height))
        blur-height   (reanimated/use-shared-value (+ constants/composer-default-height
                                                      (:bottom insets)))
        extra-params  {:insets        insets
                       :window-height window-height
                       :blur-height   blur-height
                       :opacity       opacity
                       :background-y  background-y}
        props         {:input-ref                   (atom nil)
                       :selectable-input-ref        (atom nil)
                       :keyboard-show-listener      (atom nil)
                       :keyboard-frame-listener     (atom nil)
                       :keyboard-hide-listener      (atom nil)
                       :emoji-kb-extra-height       (atom nil)
                       :saved-emoji-kb-extra-height (atom nil)
                       :replying?                   (atom false)
                       :sending-images?             (atom false)
                       :editing?                    (atom false)
                       :record-permission?          (atom false)
                       :record-reset-fn             (atom nil)
                       :scroll-y                    (atom 0)
                       :selection-event             (atom nil)
                       :selection-manager           (rn/selectable-text-input-manager)}
        state         {:text-value            (reagent/atom "")
                       :cursor-position       (reagent/atom 0)
                       :saved-cursor-position (reagent/atom 0)
                       :gradient-z-index      (reagent/atom 0)
                       :kb-default-height     (reagent/atom nil)
                       :gesture-enabled?      (reagent/atom true)
                       :lock-selection?       (reagent/atom true)
                       :focused?              (reagent/atom false)
                       :lock-layout?          (reagent/atom false)
                       :maximized?            (reagent/atom false)
                       :recording?            (reagent/atom false)
                       :first-level?          (reagent/atom true)
                       :menu-items            (reagent/atom selection/first-level-menu-items)}]
    [rn/view
     [reanimated/view {:style (style/background opacity background-y window-height)}]
     [sub-view/blur-view blur-height]
     [:f> sheet-component extra-params props state]]))