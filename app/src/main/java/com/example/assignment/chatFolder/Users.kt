package com.example.assignment.chatFolder

class Users {

    var userName:String?=null
    var userEmail:String?=null
    var uid:String?=null
    var userImage:String?=null
    constructor(){}
    constructor(userName:String?,userEmail:String?,uid:String?,userImage:String?){
        this.userName=userName
        this.userEmail=userEmail
        this.uid=uid
        this.userImage=userImage
    }

}