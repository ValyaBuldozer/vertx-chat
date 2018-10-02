import * as React from "react";
import {Button, TextField} from "@material-ui/core";
import {observable} from "mobx";
import {observer} from "mobx-react";
import {Component} from "react";
import "../styles.css";
import {string} from "prop-types";

interface  IFormProps {
    onLoginHandler : Function;
}

@observer
export class InputForm extends Component<IFormProps> {

    @observable username : string;

    handleChange = (event) => {
        this.username = event.target.value;
    }


    render() {
        const {onLoginHandler} = this.props;
        return (
            <div className="inputRootDiv">
                <TextField value={this.username}
                           onChange={(e) => this.handleChange(e)}/>
                <Button onClick={(e) => onLoginHandler(this.username)}
                        variant={"text"}>
                    {"login"}
                </Button>
            </div>
        );
    }
}