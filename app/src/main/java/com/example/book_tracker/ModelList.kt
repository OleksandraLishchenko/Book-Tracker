package com.example.book_tracker

class ModelList {
    var id: String = ""
    var list: String = ""
    var timestamp: Long = 0
    var uid: String = ""

    constructor()
    constructor(id: String, list: String, timestamp: Long, uid: String, isPermanent: Boolean = false) {
        this.id = id
        this.list = list
        this.timestamp = timestamp
        this.uid = uid
    }
}
