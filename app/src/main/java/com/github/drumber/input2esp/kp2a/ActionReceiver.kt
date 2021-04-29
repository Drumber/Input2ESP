package com.github.drumber.input2esp.kp2a

import android.content.Intent
import android.widget.Toast
import com.github.drumber.input2esp.MainActivity
import com.github.drumber.input2esp.R
import keepass2android.pluginsdk.PluginAccessException
import keepass2android.pluginsdk.Strings
import org.json.JSONObject

class ActionReceiver: keepass2android.pluginsdk.PluginActionBroadcastReceiver() {

    companion object {
        const val ACTION_ID = "com.github.drumber.input2esp.send";
    }

    override fun openEntry(oe: OpenEntryAction?) {
        if(oe == null) return

        val context = oe.context
        try {
            oe.addEntryAction(context.getString(R.string.action_send_to_esp), R.drawable.ic_keyboard, null)

            oe.entryFields.entries.forEach { (field, _) ->
                oe.addEntryFieldAction(ACTION_ID, Strings.PREFIX_STRING + field, context.getString(R.string.action_send_to_esp), R.drawable.ic_keyboard, null)
            }
        } catch (e: PluginAccessException) {
            e.printStackTrace()
            Toast.makeText(context, "An Error occurred: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun actionSelected(actionSelected: ActionSelectedAction?) {
        actionSelected?.context?.let {
            val intent = Intent(it, MainActivity::class.java)
            intent.apply {
                putExtra(Strings.EXTRA_ENTRY_OUTPUT_DATA, JSONObject(actionSelected.entryFields as Map<*, *>).toString())
                putExtra(Strings.EXTRA_ENTRY_ID, actionSelected.entryId)
                putExtra(Strings.EXTRA_FIELD_ID, actionSelected.fieldId)
                putExtra(Strings.EXTRA_SENDER, actionSelected.hostPackage)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            it.startActivity(intent)
        }
    }

    override fun entryOutputModified(eom: EntryOutputModifiedAction?) {
        if(eom == null) return

        val context = eom.context
        try {
            eom.addEntryFieldAction(ACTION_ID, eom.modifiedFieldId,context.getString(R.string.action_send_to_esp), R.drawable.ic_keyboard, null)
        } catch (e: PluginAccessException) {
            e.printStackTrace()
            Toast.makeText(context, "An Error occurred: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

}