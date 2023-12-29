package im.status.ethereum.module

import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp
import com.facebook.react.uimanager.events.RCTEventEmitter
import com.facebook.react.views.textinput.ReactEditText
import com.facebook.react.views.view.ReactViewGroup
import com.facebook.react.views.view.ReactViewManager
import java.util.ArrayList

class RNSelectableTextInputViewManager : ReactViewManager() {
    companion object {
        const val REACT_CLASS = "RNSelectableTextInput"
    }

    private var _menuItems: Array<String> = arrayOf()

    override fun getName(): String {
        return REACT_CLASS
    }

    override fun createViewInstance(context: ThemedReactContext): ReactViewGroup {
        return ReactViewGroup(context)
    }

    @ReactProp(name = "menuItems")
    fun setMenuItems(reactViewGroup: ReactViewGroup, items: ReadableArray?) {
        if (items != null) {
            val result = ArrayList<String>(items.size())
            for (i in 0 until items.size()) {
                result.add(items.getString(i))
            }
            _menuItems = result.toTypedArray()
        }
    }

    fun registerSelectionListener(view: ReactEditText) {
        view.setCustomSelectionActionModeCallback(object : ActionMode.Callback {
            override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
                menu.clear()
                for (i in _menuItems.indices) {
                    menu.add(0, i, 0, _menuItems[i])
                }
                return true
            }

            override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                return true
            }

            override fun onDestroyActionMode(mode: ActionMode) {}

            override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                val selectionStart = view.selectionStart
                val selectionEnd = view.selectionEnd
                val selectedText = view.text.toString().substring(selectionStart, selectionEnd)

                // Dispatch event
                onSelectNativeEvent(view, item.itemId, selectedText, selectionStart, selectionEnd)

                mode.finish()

                return true
            }
        })
    }

    private fun onSelectNativeEvent(view: ReactEditText, eventType: Int, content: String, selectionStart: Int, selectionEnd: Int) {
        val event: WritableMap = Arguments.createMap()
        event.putInt("eventType", eventType)
        event.putString("content", content)
        event.putInt("selectionStart", selectionStart)
        event.putInt("selectionEnd", selectionEnd)

        // Dispatch
        val reactContext = view.context as ReactContext
        reactContext.getJSModule(RCTEventEmitter::class.java).receiveEvent(view.id, "topSelection", event)
    }

    override fun getExportedCustomDirectEventTypeConstants(): Map<String, Any> {
        return mapOf("topSelection" to mapOf("registrationName" to "onSelection"))
    }
}
