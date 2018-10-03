import * as React from "react";
import {Typography} from "@material-ui/core";
import "../styles.css";

export const ChatMessage = ({message, username}) => {
    return(
        <div className="messageDiv">
            <Typography variant="body2" gutterBottom>
                {username}
            </Typography>
            <Typography variant="body1" gutterBottom>
                {message}
            </Typography>
        </div>
    )
}