export interface IMessage {
    type : string;
    username : string;
    text : string;
}

export class Message implements IMessage {
    public type : string;
    public username : string;
    public text : string;

    constructor(type : string, username : string = "", message : string = "") {
        this.username = username;
        this.text = message;
        this.type = type;
    }
}