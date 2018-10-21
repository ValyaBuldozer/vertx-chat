import * as React from "react";
import {List, ListItem, Paper, Typography} from "@material-ui/core";
import "../styles.css";
import { connect } from "react-redux";
import {IState} from "../store";
import Client from "../Models/client";

interface IClientsProps {
    clientsList: Array<Client>
}

const ClientsList : React.StatelessComponent<IClientsProps> = ({clientsList}) => {
    console.log("clients");
    console.log(clientsList);
    return (
        <Paper elevation={1} className="userListRoot">
            <div className="clientsListBase">
                <Typography variant={"caption"}>
                    Clients
                </Typography>
                <List component="nav">
                {clientsList.map(client => {
                    return (
                        <ListItem button key={client.id}>
                            {client.username}
                        </ListItem>
                    )})}
            </List>
            </div>
        </Paper>
    )
};

export const Clients = connect(
    (state  : IState) => ({
        clientsList : state.clients
    } as IClientsProps)
)(ClientsList);
