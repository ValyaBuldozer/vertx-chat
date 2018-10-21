import * as React from "react";
import {Button, TextField, Paper} from "@material-ui/core";
import { connect } from "react-redux";
import {Component} from "react";
import {socketConnection, socketConnection as connection} from "../Network/SocketConnection";
import "../styles.css";
import { logIn, addClient } from "../Reducers/reducersCreactor";
import {IState} from "../store";
import {client} from "../Reducers/chatReducers";

interface  IFormProps {
    username : string;
    onSubmit : Function;
    onLogin : Function;
    isAuthorized : boolean;
}

interface IFormState {
    message : string
}

class InputForm extends Component<IFormProps, IFormState> {
    constructor(props) {
        super(props);
        this.state = {
            message : ""
        }
    }

    handleChange = (event) => {
        this.setState({
            ...this.state,
            message : event.target.value
        });
    }

    render() {
        const {onSubmit, onLogin, isAuthorized} = this.props;
        return (
            <div className="inputRootDiv">
                <Paper className="loginPaper" elevation={4}>
                    <TextField  style={{width: 400}}
                                value={this.state.message}
                                onChange={(e) => this.handleChange(e)}
                                label={isAuthorized ? "Message" : "Username"}/>
                    <Button onClick={() => isAuthorized ?
                                            onSubmit(this.state.message) :
                                            onLogin(this.state.message)}
                            variant={"text"}>
                        {isAuthorized ? "SEND" : "LOGIN"}
                    </Button>
                </Paper>
            </div>
        );
    }
}

export const Input = connect(
    (state : IState )=> ({
        username : state.user.username,
        isAuthorized : state.user.isAuthorized
    }),
    dispatch => ({
        onSubmit(message) {
            connection.sendMessageFromToken(message);
        },
        onLogin(username : string) {
            //dispatch(logIn(username));
            console.log("authorize request " + username);
            const request = new XMLHttpRequest();
            request.open('POST', "/authorize");
            request.onload = () => {
                if(request.readyState === 4) {
                    if (request.status === 200) {
                        const response = JSON.parse(request.response);
                        //socketConnection.sendMessageToAddress(response.token, "test");
                        socketConnection.token = response.token;
                        dispatch(logIn(username));
                        response.users.forEach((user) => {
                            dispatch(addClient(user));
                        });
                    } else {
                        console.log(request.statusText);
                    }
                }
            };
            request.onerror = () => {
                console.log(request.responseText);
            };

            request.send(JSON.stringify({username : username}));

        }
    })
)(InputForm);