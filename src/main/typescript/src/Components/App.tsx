import * as React from "react";
import { Component } from "react";
import { connect } from "react-redux";
import {Input} from "./InputFrom";
import { Chat } from "./ChatHistory";
import { addMessage } from "../Reducers/reducersCreactor";
import {socketConnection as connection} from "../Network/SocketConnection";


interface IAppProps {
    username : string;
    onMessageHandled : Function;
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

    render() {
        console.log("render");
        return (
            <div>
                <Chat />
                <Input/>
            </div>
        )
    }
}

export const App = connect(
    state => ({
        username : state.username
    }),
    dispatch => ({
        onMessageHandled(message) {
            dispatch(addMessage(message));
        }
    })
)(AppComponent);