import * as React from "react";
import ChatMessage from "./ChatMessage";
import { connect } from "react-redux";
import {IState} from "../store";
import {Paper} from "@material-ui/core";

const ChatHistory = ({messages}) => {
    console.log("messages");
    console.log(messages);
    return(
        <Paper className="chatHistoryRoot" elevation={1}>
            {messages.map((msg) => {
                return(
                    <ChatMessage {...msg} key={msg.id}/>
                )
            })}
        </Paper>
    )
}

export const Chat = connect(
    (state :IState ) =>
        ({
            messages : state.messages
        })
)(ChatHistory);