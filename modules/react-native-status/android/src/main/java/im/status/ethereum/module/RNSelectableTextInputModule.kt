package im.status.ethereum.module

import android.view.ActionMode
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.uimanager.NativeViewHierarchyManager
import com.facebook.react.uimanager.UIBlock
import com.facebook.react.uimanager.UIManagerModule
import com.facebook.react.views.textinput.ReactEditText

class RNSelectableTextInputModule(private val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    private var lastActionMode: ActionMode? = null

    override fun getName(): String {
        return "RNSelectableTextInputManager"
    }

    @ReactMethod
    fun setupMenuItems(selectableTextViewReactTag: Int?, textInputReactTag: Int?) {
        val uiManager = reactContext.getNativeModule(UIManagerModule::class.java)
        uiManager?.addUIBlock(object : UIBlock {
            override fun execute(nvhm: NativeViewHierarchyManager) {
                selectableTextViewReactTag?.let { selectableTag ->
                    textInputReactTag?.let { inputTag ->
                        val rnSelectableTextManager = nvhm.resolveViewManager(selectableTag) as RNSelectableTextInputViewManager
                        val reactTextView = nvhm.resolveView(inputTag) as ReactEditText
                        rnSelectableTextManager.registerSelectionListener(reactTextView)
                    }
                }
            }
        })
    }

    @ReactMethod
    fun startActionMode(textInputReactTag: Int?) {
        val uiManager = reactContext.getNativeModule(UIManagerModule::class.java)
        uiManager?.addUIBlock(object : UIBlock {
            override fun execute(nvhm: NativeViewHierarchyManager) {
                textInputReactTag?.let { inputTag ->
                    val reactTextView = nvhm.resolveView(inputTag) as ReactEditText
                    lastActionMode = reactTextView.startActionMode(reactTextView.customSelectionActionModeCallback, ActionMode.TYPE_FLOATING)
                }
            }
        })
    }

    @ReactMethod
    fun hideLastActionMode() {
        val uiManager = reactContext.getNativeModule(UIManagerModule::class.java)
        uiManager?.addUIBlock(object : UIBlock {
            override fun execute(nvhm: NativeViewHierarchyManager) {
                lastActionMode?.finish()
                lastActionMode = null
            }
        })
    }

    @ReactMethod
    fun setSelection(textInputReactTag: Int?, start: Int?, end: Int?) {
        val uiManager = reactContext.getNativeModule(UIManagerModule::class.java)
        uiManager?.addUIBlock(object : UIBlock {
            override fun execute(nvhm: NativeViewHierarchyManager) {
                textInputReactTag?.let { inputTag ->
                    val reactTextView = nvhm.resolveView(inputTag) as ReactEditText
                    reactTextView.setSelection(start ?: 0, end ?: 0)
                }
            }
        })
    }
}
