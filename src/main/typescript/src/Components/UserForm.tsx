import * as React from 'react';
import {Button, Paper, TextField} from "@material-ui/core";
import {connect} from "react-redux";
import {IState} from "../store";
import {authorizeRequest} from "../Network/RestService";
import {logIn, addClients} from "../Reducers/reducersCreactor";
import {socketConnection} from "../Network/SocketConnection";

interface UserFormProps {
    loginHandler: (username: string, password: string) => void;
    registrationHandler?: (username: string, password: string) => void;
}

interface UserFormState {
    regMode: boolean;
    username: string;
    password: string;
}

class UserFormComponent extends React.Component<UserFormProps, UserFormState> {
    constructor(props) {
        super(props);

        this.state = {
            regMode: false,
            username: "",
            password: ""
        }
    }

    handleChange = field => event => {
        this.setState({
            ...this.state,
            [field]: event.target.value
        });
    }

    render() {
        const {regMode, username, password} = this.state;
        const {loginHandler, registrationHandler} = this.props;

        return (
            <Paper elevation={5} className="user-form-root">
                <TextField value={username}
                    onChange={this.handleChange("username")}
                    label="Username"/>
                <TextField value={password}
                    onChange={this.handleChange("password")}
                    label="Password"/>
                <Button variant="text"
                    onClick={() => loginHandler(username, password)}>
                    Login
                </Button>

            </Paper>
        )
    }
}

export const UserForm = connect(
    (state : IState) => ({

    }), (dispatch) => ({
        loginHandler: (username: string, password: string) => {
            authorizeRequest(username, password).then((response) => {
                const {users, token} = JSON.parse(response.toString());
                dispatch(addClients(users));
                dispatch(logIn(username));
                socketConnection.token = token;
            }).catch(err => console.log(err))
        }
    })
)(UserFormComponent);