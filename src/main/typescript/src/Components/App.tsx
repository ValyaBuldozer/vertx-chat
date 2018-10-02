import * as React from "react";
import { Component } from "react";
import {IObservableArray, observable} from "mobx";
import { observer } from "mobx-react";
import * as EventBus from "vertx3-eventbus-client";
import {Button, TextField} from "@material-ui/core";
import SocketConnection from "../Network/SocketConnection"
import {InputForm} from "./InputFrom";

@observer
export class App extends Component {

    @observable private messages;
    @observable private username;

    private connection  : SocketConnection;


    constructor(props) {
        super(props);
        this.messages = observable([]);
        this.connection = new SocketConnection("client.to.server");
        this.connection.onpublish = (username, message) => {
            console.log(username);
            console.log(message);
        }
    }

    handleChange(event) {
        this.username = event.target.value;
    };
    clickHandler() {
        this.connection.sendMessage(this.username);
    }

    render() {
        console.log("render");
        return (
            <div>
                
                <InputForm onLoginHandler={() => this.clickHandler()}/>
            </div>
        )
    }
}