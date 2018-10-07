import * as React from "react";
import ChatMessage from "./ChatMessage";
import { connect } from "react-redux";

const ChatHistory = ({messages}) => {

    return(
        <div>
            {messages.map((msg) => {
                return(
                    <ChatMessage text={msg.text} username={msg.username} key={msg.id}/>
                )
            })}
        </div>
    )
}

export const Chat = connect(
    state =>
        ({
            messages : state.messages
        })
)(ChatHistory);