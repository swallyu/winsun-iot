package com.winsun.iot.utils.tree;

import com.winsun.iot.command.CommandHandler;
import com.winsun.iot.command.EnumQoS;

import java.util.ArrayList;
import java.util.List;

public class TriaTree<T> {

    private TreeNode<T> rootNode=new TreeNode(null);

    public void add(String topic,T commandInfo){
        String[] topics = topic.split("/");

        rootNode.addChild(topics,commandInfo);
    }

    public List<T> getValue(String topic){
        String[] topics = topic.split("/");

        List<T> list = new ArrayList<>();
        rootNode.getMatch(topics,list);
        return list;
    }

    public static void main(String[] args) {
        CommandHandler cmd1=new CommandHandler("/e2es/+/+", EnumQoS.Once,null);
        CommandHandler cmd2=new CommandHandler("/e2es/#", EnumQoS.Once,null);
        CommandHandler cmd3=new CommandHandler("/e2es/sensor/125", EnumQoS.Once,null);
        CommandHandler cmd4=new CommandHandler("/e2es/gateway/125", EnumQoS.Once,null);

        TriaTree<CommandHandler> tree = new TriaTree<>();
        tree.add(cmd1.getTopic(),cmd1);
        tree.add(cmd2.getTopic(),cmd2);

        List<CommandHandler> commandHandlers = tree.getValue("/e2es/sensor/123");
        for (CommandHandler commandHandler : commandHandlers) {
            System.out.println(commandHandler.getTopic());
        }
    }
}
