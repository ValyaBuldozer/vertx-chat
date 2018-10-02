import * as EventBus from "vertx3-eventbus-client";
import {IMessage} from "../Classes/IMessage";

export default class SocketConnection {
    private eventbus : EventBus;

    public onpublish : Function;
    public ondisconnect : Function;
    public onregister : Function;

    private readonly eventbusaddress : string;

    constructor(eventbusAddress : string, ) {
        this.eventbusaddress = eventbusAddress;
        this.eventbus = new EventBus("/eventbus/");

        this.eventbus.onopen = () => {
            this.eventbus.registerHandler(this.eventbusaddress,
                (err, msg) => {this.handleMessage(err, msg)});
        }
    }

    handleMessage(err, msg) {
        if(err) {
            console.log(err);
        }

        const message : IMessage = msg.body;

        switch (message.type) {
            case "publish":
                if (this.onpublish) {
                    this.onpublish(message.username, message.message);
                }
                break;
            case "user_disconnect":
                if (this.ondisconnect) {
                    this.ondisconnect(message.username);
                }
                break;
            case "user_connected":
                if (this.onregister) {
                   this.onregister(message.username);
                }
            default:
                break;
        }
    }

    sendMessage(message : string) {
        this.eventbus.publish(this.eventbusaddress, message);
    }
}