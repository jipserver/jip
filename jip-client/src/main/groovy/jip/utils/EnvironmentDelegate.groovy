package jip.utils

/**
 * Closure delegate that can be used to extend MAP easily in a bash like syntax.
 * <p>
 *     For example, a closeure like :
 *     <pre><code>
 *     {
 *         PATH="${PATH}:/sbin"
 *     }
 *     <code></pre>
 *     will either create or extend PATH. Non existing variables are set to empty string on first
 *     access.
 * </p>
 *
 *
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
class EnvironmentDelegate {
    Map<String, String> data

    /**
     * Create a new environment delegate
     *
     * @param data the data
     */
    EnvironmentDelegate(Map<String, String> data) {
        if(data == null){
            data = new HashMap<String, String>()
        }
        this.data = data
    }

    def propertyMissing(String name, value) {
        if(value == null){
            data.remove(name)
        }else{
            data[name] = value.toString()
        }
    }

    def propertyMissing(String name) {
        if (!data.containsKey(name)){
            data[name] = ""
        }
        return data[name]
    }
}
