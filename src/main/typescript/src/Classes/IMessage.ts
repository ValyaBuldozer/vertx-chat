export interface IMessage {
    type : string;
    username : string;
    message : string;
}

export class Message implements IMessage {
    public type : string;
    public username : string;
    public message : string;

    constructor(type : string, username : string = "", message : string = "") {
        this.username = username;
        this.message = message;
        this.type = type;
    }
}