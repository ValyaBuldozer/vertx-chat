import * as React from "react";
import {Button, TextField, Paper} from "@material-ui/core";
import {observable} from "mobx";
import {observer} from "mobx-react";
import {Component} from "react";
import "../styles.css";

interface  IFormProps {
    onSubmit : Function;
    onLogin : Function;
    isAuthorized : boolean;
}

@observer
export class InputForm extends Component<IFormProps> {

    @observable message : string;

    handleChange = (event) => {
        this.message = event.target.value;
    }

    render() {
        const {onSubmit, onLogin, isAuthorized} = this.props;
        return (
            <div className="inputRootDiv">
                <Paper className="loginPaper">
                    <TextField  style={{width: 400}}
                                value={this.message}
                                onChange={(e) => this.handleChange(e)}
                                label={isAuthorized ? "Message" : "Username"}/>
                    <Button onClick={() => isAuthorized ?
                                            onSubmit(this.message) :
                                            onLogin(this.message)}
                            variant={"text"}>
                        {isAuthorized ? "SEND" : "LOGIN"}
                    </Button>
                </Paper>
            </div>
        );
    }
}