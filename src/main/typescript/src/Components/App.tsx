import * as React from "react";
import { Component } from "react";
import {IObservableArray, observable} from "mobx";
import { observer } from "mobx-react";
import * as EventBus from "vertx3-eventbus-client";
import {Button, TextField} from "@material-ui/core";
import SocketConnection from "../Network/SocketConnection"
import {InputForm} from "./InputFrom";
import {string} from "prop-types";
import {Message} from "../Classes/IMessage";
import {ChatHistory} from "./ChatHistory";

@observer
export class App extends Component {

    @observable private messages = new Array<Message>();
    @observable private username : string;
    @observable private isAuthorized : boolean;

    private connection  : SocketConnection = new SocketConnection();

    constructor(props) {
        super(props);

    }

    componentWillMount() {
        if(!this.connection.isConnected()) {
            this.setUpSocketConnection(this.messageHandler)
        }
    }

    private setUpSocketConnection(messageHandler : Function) {
        this.loadConfigs(({url, ebaddress}) => {
            this.connection.connect(url, ebaddress);
            this.connection.onpublish = (username, message) => {
                messageHandler.apply(this, [username, message]);
            };
        });
    }

    private loadConfigs(callback : Function) {
        const request = new XMLHttpRequest();
        request.open('GET', "/socketconfig");
        request.onload = () => {
            if(request.readyState === 4) {
                if (request.status === 200) {
                    callback(JSON.parse(request.response));
                } else {
                    console.log(request.statusText);
                }
            }
        };
        request.onerror = () => {
            console.log(request.responseText);
        }

        request.send(null);
    }

    messageHandler(username : string, message : string) {
        this.messages.push(new Message("publish",username,  message));
    }

    handleLogin(username : string) {
        this.username = username;
        this.isAuthorized = true;
    }

    onSubmit(message : string) {
        this.connection.sendMessage(message, this.username);
    }

    render() {
        console.log("render");
        return (
            <div>
                <ChatHistory messages={this.messages}/>
                <InputForm onSubmit={(msg) => this.onSubmit(msg)}
                            isAuthorized={this.isAuthorized}
                            onLogin={(username) => this.handleLogin(username)}/>
            </div>
        )
    }
}