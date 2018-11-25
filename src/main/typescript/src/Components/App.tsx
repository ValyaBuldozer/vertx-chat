import * as React from "react";
import { Component } from "react";
import { connect } from "react-redux";
import {Input} from "./InputFrom";
import { Chat } from "./ChatHistory";
import { addMessage, addClient } from "../Reducers/reducersCreactor";
import {socketConnection as connection} from "../Network/SocketConnection";
import { Clients } from "./ClientsList";
import {IState} from "../store";
import {socketConfigRequest} from "../Network/RestService";
import {UserForm} from "./UserForm";


interface IAppProps {
    username : string;
    isAuthorized : boolean;
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
        socketConfigRequest().then((responce) => {
            const {url, ebaddress} = JSON.parse(responce.toString());
            connection.connect(url, ebaddress);
            connection.onpublish = (username, text) => {
                messageHandler({username, text});
            };
            connection.onregister = (username) => {
                this.props.onUserRegistered(username);
            }
        }).catch(err => console.error(err))
    }

    render() {
        if (this.props.isAuthorized) {
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

        return (
            <UserForm/>
        )
    }
}

export const App = connect(
    ( state : IState) => ({
        ...state.user
    } as IAppProps),
    dispatch => ({
        onMessageHandled(message) {
            dispatch(addMessage(message));
        },
        onUserRegistered(username) {
            dispatch(addClient(username));
        }
    })
)(AppComponent);