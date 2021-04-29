package com.github.drumber.input2esp.backend.placeholders

abstract class PlaceholderHandler: Comparable<PlaceholderHandler> {

    val PRIORITY_NORMAL = 20
    val PRIORITY_LOW = 10
    val PRIORITY_HIGH = 50

    /**
     * Handle the occurrence of the placeholder this Handler is registered for.
     *
     * @param placeholder   the placeholder without closures
     * @param range         the start and end index of the found placeholder (closures included)
     * @param text          the whole input text
     * @param processor     processor instance for actions
     */
    abstract fun processPlaceholder(placeholder: String, range: IntRange, text: String, processor: Processor)

    /**
     * Can the placeholder be used and cam it be displayed to the user.
     * @param placeholder   the placeholder without closures to check
     * @return  default true, return false if the placeholder can't be used
     *          (e.g. password is not available)
     */
    open fun isPlaceholderSupported(placeholder: String): Boolean = true

    /**
     * Get the priority for this handler. Handlers with higher priority will
     * be shown higher in the list.
     * @return  priority as integer (must be positive)
     */
    open fun getPriority(): Int = PRIORITY_NORMAL

    /**
     * Optional category for placeholders associated with this handler.
     * If no category is defined, the placeholders of this handler will be
     * added to the 'Others' category.
     * @return  name of the category or null
     */
    open fun getCategory(): String? = null

    override fun compareTo(other: PlaceholderHandler): Int {
        return getPriority() - other.getPriority()
    }
}