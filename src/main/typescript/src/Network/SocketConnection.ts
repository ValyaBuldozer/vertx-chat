import * as EventBus from "vertx3-eventbus-client";

class SocketConnection {
    private eventbusaddress : string;
    private eventbus : EventBus;

    public token : string;
    public onpublish : Function;
    public ondisconnect : Function;
    public onregister : Function;

    public connect(eventBusUrl : string, eventBusAddress : string) {
        this.eventbusaddress = eventBusAddress;
        this.eventbus = new EventBus(eventBusUrl);
        this.eventbus.onopen = () => {
            this.eventbus.registerHandler(this.eventbusaddress,
                (err, msg) => {this.handleMessage(err, msg)});
        }
    }

    public disconnect() {
        if (this.eventbus) {
            this.eventbus.close();
        } else {
            throw new Error("Trying to close not open connection");
        }
    }

    public close() {
        if(this.token) {
            this.eventbus.publish();
            this.eventbus.publish(this.eventbusaddress, {"type" : "disconnect"}, {"token" : this.token});
        }
    }

    public isConnected() {
        return (this.eventbus);
    }

    handleMessage(err, msg) {
        if(err) {
            console.log(err);
        }

        const message = msg.body;

        switch (message.action) {
            case "new-message":
                if (this.onpublish) {
                    this.onpublish(message.username, message.text);
                }
                break;
            case "user-logout":
                if (this.ondisconnect) {
                    this.ondisconnect(message.username);
                }
                break;
            case "user-login":
                if (this.onregister) {
                   this.onregister(message.username);
                }
                break;
            default:
                break;
        }
    }

    sendMessageFromToken(message : string) {
        this.eventbus.publish(this.eventbusaddress,
            message,
            {
                "type" : "message",
                "token" : this.token
            });
    }
}

export const socketConnection = new SocketConnection();