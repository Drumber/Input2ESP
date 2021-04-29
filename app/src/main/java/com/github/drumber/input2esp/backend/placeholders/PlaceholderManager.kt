package com.github.drumber.input2esp.backend.placeholders

import com.github.drumber.input2esp.R
import com.github.drumber.input2esp.backend.placeholders.handlers.DatePlaceholder
import com.github.drumber.input2esp.backend.placeholders.handlers.ModifierHandler
import com.github.drumber.input2esp.backend.utils.CommonUtils
import java.util.*
import kotlin.math.max

class PlaceholderManager {

    data class Closure(val head: CharSequence, val tail: CharSequence = head)

    private val defaultClosure = Closure("%")
    private val placeholders = mutableMapOf<String, PlaceholderHandler>()

    var closure: Closure = defaultClosure

    init {
        registerDefaultHandlers()
        Char.MAX_VALUE
    }

    fun registerPlaceholder(placeholderText: String, handler: PlaceholderHandler) {
        placeholders[placeholderText.lower()] = handler
    }

    fun unregisterPlaceholder(placeholderText: String) {
        placeholders.remove(placeholderText)
    }

    fun isRegistered(handler: PlaceholderHandler) = placeholders.values.contains(handler)

    /**
     * Parse the text by extracting the placeholders, replacing the placeholder with corresponding
     * keycode(s) and adding everything to a command list. The command list consists of the ASCII
     * keycodes from the specified string.
     * @param text  input text that may include placeholders separated by the configured closures
     * @return      processed text as a list of ASCII codes
     */
    fun processText(text: String): List<Int> {
        val regex = "${closure.head}([\\w-]+?)${closure.tail}".toRegex() // find words between closures (also supports '_' and '-')

        val commandList = mutableListOf<Int>()

        var blockStart = 0
        var blockEnd: Int

        var nextResult: MatchResult? = regex.find(text)
        while (nextResult != null) {

            // get the first group of our regular expression (= any content between closures)
            val group = nextResult.groups[1]

            if(group != null) {
                //println("Next result: ${group.value}")

                // we add the text starting from blockStart, ending at the next found placeholder to the command list.
                // nextResult.range includes the closures. The group 1 does only include the placeholder text between the closures
                blockEnd = nextResult.range.first
                if(blockEnd > blockStart) {
                    val nonPlaceholderBlock = text.substring(blockStart, blockEnd)
                    commandList.addAll(KeyCode.fromString(nonPlaceholderBlock))
                }
                // set blockStart to the first char index after the placeholder
                blockStart = nextResult.range.last + 1

                // get the handler for the found placeholder
                val handler = getHandler(group.value)
                if(handler != null) {
                    // create new processor instance for this handler
                    val processor = Processor()

                    handler.processPlaceholder(group.value, group.range, text, processor)

                    // add the key codes added by the handler to the command list
                    commandList.addAll(processor.keycodes)
                } else {
                    // if we don't find a handler, we may interpret the substring as part of the message
                    // so add it to the command list
                    val substring = text.substring(nextResult.range)
                    commandList.addAll(KeyCode.fromString(substring))
                }
            }

            nextResult = nextResult.next()
        }

        // set blockEnd to the last character position
        blockEnd = text.length
        // if there is text left after the last placeholder (end > start>, we add it to the command list
        if(blockEnd > blockStart) {
            val substring = text.substring(blockStart, blockEnd)
            commandList.addAll(KeyCode.fromString(substring))
        }

        // for debugging
        //val recreatedString = KeyCode.buildString(commandList)
        //println("###### [command] recreated string: $recreatedString")

        return commandList
    }

    fun getHandler(placeholderText: String, ignoreUnsupported: Boolean = false): PlaceholderHandler? {
        if(ignoreUnsupported) return placeholders[placeholderText.lower()]
        return getSupportedHandlers()[placeholderText.lower()]
    }

    fun getSupportedHandlers() = placeholders.filter { it.value.isPlaceholderSupported(it.key) }

    /**
     * Get all supported placeholders, ordered by the priority of the corresponding handler.
     */
    fun getPlaceholders() = getSupportedHandlers().toList()
            .sortedBy { (key, value) -> value.getPriority() }
            .reversed().toMap().keys

    /**
     * Get all groups and the corresponding placeholders of the group as a HashMap.
     * The Map will be sorted by the highest priority of a handler that is in the group.
     * Placeholder handlers that have not specified a category will be added to the
     * 'Others' group.
     * @return  groups with placeholders as a HashMap where the key is the group name
     *          and the value is the list of placeholders.
     */
    fun getPlaceholderGroups(): Map<String, MutableList<String>> {
        val othersGroupName = CommonUtils.getStringResource(R.string.placeholder_category_others)
        val groups = linkedMapOf<String, MutableList<String>>()
        val groupPriority = mutableMapOf<String, Int>()

        placeholders.entries.forEach { entry ->
            val category = entry.value.getCategory()

            if(category != null) { // custom category
                val itemList = groups[category] ?: mutableListOf()
                itemList.add(entry.key) // add placeholder to group items
                groups[category] = itemList

                // set the highest priority for the group
                val prevPriority = groupPriority[category] ?:0
                groupPriority[category] = max(prevPriority, entry.value.getPriority())
            } else { // add to 'others' group
                val itemList = groups[othersGroupName] ?: mutableListOf()
                itemList.add(entry.key) // add placeholder to group items
                groups[othersGroupName] = itemList

                // set the highest priority for the group
                val prevPriority = groupPriority[othersGroupName] ?:0
                groupPriority[othersGroupName] = max(prevPriority, entry.value.getPriority())
            }
        }

        // sort groups by priority
        return groups.toList().sortedBy { groupPriority[it.first] }.reversed().toMap()
    }

    private fun String.lower() = this.toLowerCase(Locale.getDefault())

    private fun registerDefaultHandlers() {
        DatePlaceholder().apply {
            registerPlaceholder(DATE, this)
            registerPlaceholder(TIME, this)
        }
        ModifierHandler().register(this)
    }

}