class UnsupportedQuery(val query: String) :
    Exception("No query named $query") {
}

class IncorrectNumberOfArguments(val provided: Int, val required: Int, val query: String) :
    Exception("$query requires $required number of arguments, but $provided provided") {
}

class EmptyLine :
    Exception("No query") {
}

class KeyAlreadyExists(val key: String) :
    Exception("Key $key already exists") {
}

class NoSuchKey(val key: String) :
    Exception("Key $key doesn't exists") {
}