package com.winsun.iot.utils.tree;

import java.util.*;

public class TreeNode<T> {
    private String nodeName;
    private List<T> value = new ArrayList<>();

    private MatchType matchType = MatchType.Extract;
    private Map<String, TreeNode<T>> child = new HashMap<>();

    public TreeNode(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public List<T> getValue() {
        return new ArrayList<>(value);
    }

    private void addValue(T commandInfo) {
        this.value.add(commandInfo);
    }


    public Map<String, TreeNode<T>> getChild() {
        return child;
    }

    public void setChild(Map<String, TreeNode<T>> child) {
        this.child = child;
    }

    public void addChild(String[] topic, T commandInfo) {
        if (topic.length > 0) {
            if (topic.length == 1) {
                TreeNode<T> childNode = child.computeIfAbsent(topic[0], k -> new TreeNode<T>(k));
                if(Objects.equals(topic[0],"#")){
                    childNode.matchType=MatchType.Full;
                }else if(Objects.equals(topic[0],"+")){
                    childNode.matchType=MatchType.Level;
                }
                childNode.addValue(commandInfo);
            } else {
                TreeNode<T> childNode = child.computeIfAbsent(topic[0], k -> new TreeNode<T>(k));
                if(Objects.equals(topic[0],"#")){
                    childNode.matchType=MatchType.Full;
                    this.value.add(commandInfo);
                }else if(Objects.equals(topic[0],"+")){
                    childNode.matchType=MatchType.Level;
                }
                String[] childTopics = Arrays.copyOfRange(topic, 1, topic.length);
                childNode.addChild(childTopics, commandInfo);
            }
        }
    }

    public void getMatch(String[] topics, List<T> list) {
        if(this.nodeName==null){//根节点
            this.child.forEach((k,v)->{
                v.getMatch(topics,list);
            });
            return;
        }
        if(topics.length==0){
            return;
        }
        if(topics.length==1){
            if(Objects.equals(this.nodeName,topics[0])||this.matchType==MatchType.Level
                    ||this.matchType==MatchType.Full){
                list.addAll(value);
                return;
            }
        }else{
            String[] childTopics = Arrays.copyOfRange(topics, 1, topics.length);
            //精确匹配
            if(Objects.equals(this.nodeName,topics[0])&&this.matchType==MatchType.Extract){
                TreeNode<T> node = child.get(topics[1]);
                if(node!=null){
                    node.getMatch(childTopics,list);
                }
                node = child.get("+");
                if(node!=null){
                    node.getMatch(childTopics,list);
                }
                node = child.get("#");
                if(node!=null){
                    node.getMatch(childTopics,list);
                }
            }else if(this.matchType==MatchType.Full){
                //多层匹配
                list.addAll(value);
            }else if(this.matchType==MatchType.Level){
                //单层匹配
                if(this.child.size()==0){
                    list.addAll(this.value);
                }else{
                    this.child.forEach((k,v)->{
                        v.getMatch(childTopics,list);
                    });
                }
            }
        }
    }
}
