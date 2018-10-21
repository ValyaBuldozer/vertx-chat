import * as React from "react";
import { Component } from "react";
import { connect } from "react-redux";
import {Input} from "./InputFrom";
import { Chat } from "./ChatHistory";
import { addMessage, addClient } from "../Reducers/reducersCreactor";
import {socketConnection as connection} from "../Network/SocketConnection";
import { Clients } from "./ClientsList";
import {IState} from "../store";


interface IAppProps {
    username : string;
    onMessageHandled : Function;
    onUserRegistered : Function;
}

class AppComponent extends Component<IAppProps> {
    constructor(props) {
        super(props);
    }

    componentWillMount() {
        if(!connection.isConnected()) {
            this.setUpSocketConnection(this.props.onMessageHandled);
        }
    }

    private setUpSocketConnection(messageHandler : Function) {
        this.loadConfigs(({url, ebaddress}) => {
            connection.connect(url, ebaddress);
            connection.onpublish = (username, text) => {
                messageHandler({username, text});
            };
            connection.onregister = (username) => {
                this.props.onUserRegistered(username);
            }
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
        };

        request.send(null);
    }

    render() {
        console.log("render");
        return (
            <div className="appRoot">
                <div className="base">
                    <div className="clientsBase">
                        <Clients/>
                    </div>
                    <div className="messagesBase">
                        <Chat />
                    </div>
                </div>
                <Input/>
            </div>
        )
    }
}

export const App = connect(
    ( state : IState) => ({
        username : state.user.username
    }),
    dispatch => ({
        onMessageHandled(message) {
            dispatch(addMessage(message));
        },
        onUserRegistered(username) {
            dispatch(addClient(username));
        }
    })
)(AppComponent);