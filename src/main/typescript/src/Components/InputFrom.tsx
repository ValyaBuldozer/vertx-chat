import * as React from "react";
import {Button, TextField, Paper} from "@material-ui/core";
import { connect } from "react-redux";
import {Component} from "react";
import { socketConnection as connection } from "../Network/SocketConnection";
import "../styles.css";
import { logIn,addMessage } from "../Reducers/reducersCreactor";

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
                <Paper className="loginPaper">
                    <TextField  style={{width: 400}}
                                value={this.state.message}
                                onChange={(e) => this.handleChange(e)}
                                label={isAuthorized ? "Message" : "Username"}/>
                    <Button onClick={() => isAuthorized ?
                                            onSubmit({
                                                text : this.state.message,
                                                username : this.props.username}) :
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
    state => ({
        username : state.user.username,
        isAuthorized : state.user.isAuthorized
    }),
    dispatch => ({
        onSubmit(message) {
            connection.sendMessage(message);
        },
        onLogin(username : string) {
            dispatch(logIn(username));
        }
    })
)(InputForm);