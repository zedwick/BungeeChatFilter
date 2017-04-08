package com.minecraftdimensions.bungeechatfilter;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;

import java.util.HashMap;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.chat.BaseComponent;

public class Rule {

    Pattern regex;
    Pattern ignore;
    HashMap<String, String[]> actions;
    String permission = null;
    boolean needsPerm;

    public Rule( String regex, HashMap<String, String[]> actions, String permission, String ignores ) {
        this.regex = Pattern.compile( regex );
        if ( ignores == null ) {
            ignore = null;
        }    else{
        this.ignore = Pattern.compile( ignores );
        }
        this.actions = actions;
        if(permission!=null && permission.startsWith( "!" )){
            permission = permission.substring( 1,permission.length() );
            needsPerm = true;
        }
        this.permission = permission;
    }

    public Pattern getRegex() {
        return regex;
    }

    public String getStringRegex() {
        return regex.pattern();
    }

    public Matcher getMatcher( String message ) {
        return regex.matcher( message );
    }

    public boolean doesMessageContainRegex( String message ) {
        return getMatcher( message ).find();
    }

    public void performActions( ChatEvent event, ProxiedPlayer player ) {
        String message = event.getMessage();
        //get message word that matched.
        
        
        if(ignore!=null){
            Matcher ig =Pattern.compile(ignore.pattern()).matcher(message);
                while(ig.find()){
                return;
            }
        }
        for ( String action : actions.keySet() ) {
            if ( action.equals( "deny" ) ) {
                event.setCancelled( true );
            } else if ( action.equals( "message" ) ) {
            	String rp=customParams(actions.get( action )[0], player);
                player.sendMessage( TextComponent.fromLegacyText( Main.color( rp ) ) );
            } else if ( action.equals( "kick" ) ) {
            	String rp=customParams(actions.get( action )[0], player);
				
                player.disconnect( new TextComponent( TextComponent.fromLegacyText( Main.color( rp ) ) ) );
            } else if ( action.equals( "alert" ) ) {
                
                String rp=customParams(actions.get( action )[0], player);
                if(message.split( " ", 2 ).length>1){
                	rp =rp.replace("{arguments}", message.split( " ", 2 )[1] )    ;
                }
                ProxyServer.getInstance().broadcast(new TextComponent(  Main.color( rp )));
            } else if ( action.equals( "scommand" ) ) {
            	String rp=customParams(actions.get( action )[0], player);
            	
                player.chat( rp );
            } else if ( action.equals( "pcommand" ) ) {
            	String rp=customParams(actions.get( action )[0], player);
				
                ProxyServer.getInstance().getPluginManager().dispatchCommand( player, rp );
				
            } else if( action.equals( "ccommand" )){
            	String rp=customParams(actions.get( action )[0], player);
				
                ProxyServer.getInstance().getPluginManager().dispatchCommand( ProxyServer.getInstance().getConsole(), rp );
            } else if ( action.equals( "remove" ) ) {
                message = message.replaceAll( regex.pattern(), "" );
            } else if ( action.equals( "replace" ) ) {
                Random rand = new Random();
                Matcher m = getMatcher( message );
                StringBuilder sb = new StringBuilder();
                int last = 0;
                while ( m.find() ) {
                        int n = rand.nextInt( actions.get( action ).length );
                        sb.append( message.substring( last, m.start() ) );
                        sb.append( actions.get( action )[n] );
                        last = m.end();
                }
                sb.append( message.substring( last ) );
                message = sb.toString();
            } else if ( action.equals( "lower" ) ) {
                Matcher m = getMatcher( message );
                StringBuilder sb = new StringBuilder();
                int last = 0;
                while ( m.find() ) {
                        sb.append( message.substring( last, m.start() ) );
                        sb.append( m.group( 0 ).toLowerCase() );
                        last = m.end();
                }
                sb.append( message.substring( last ) );
                message = sb.toString();
            }
        }
        event.setMessage( message );
    }

    public boolean hasPermission() {
        return permission != null;
    }

    public boolean needsPermission(){
        return needsPerm;
    }

    public String getPermission() {
        return permission;
    }
    public String findRWord(String message, String regex){
    	Pattern pattern = Pattern.compile(regex);
    	Matcher m = pattern.matcher(message);
    	m.find();
    	return m.group(1) ;
    }
    public String customParams(String message, ProxiedPlayer player){
    	String match=findRWord(message, getStringRegex());
    	message=message.replace( "{player}", player.getName() )
    			.replace( "{message}", message )
    			.replace( "{match}", match );
    	
    	return message;
    }
}
