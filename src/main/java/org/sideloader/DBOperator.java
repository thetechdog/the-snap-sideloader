package org.sideloader;
import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;

public class DBOperator {
    private Connection connection;
    private Statement statement; //used for executing queries


    private final String dbPath;
    private final String jdbcUrl;

    public DBOperator(String dbPath) {
        this.dbPath=dbPath;
        this.jdbcUrl="jdbc:sqlite:"+dbPath;
    }

    public void openConnection(){
        try{if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(jdbcUrl);
            System.out.println("Database connection for repo at "+ dbPath + " opened.");
        }}
        catch (SQLException e) {
            JOptionPane.showMessageDialog(null,"Couldn't open database connection for repo at "+ dbPath+
                    ". Try refreshing the repositories, or remove the repository and add it again.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        try {
            statement = connection.createStatement();
        } catch (SQLException e) {
            System.out.println("Couldn't create statement.");
            throw new RuntimeException(e);
        }
    }

    public void closeConnection(){
        try {
            connection.close();
        } catch (SQLException e) {
            System.out.println("Couldn't close connection.");
            throw new RuntimeException(e);
        }
    }

    public ArrayList[] queryExecutorBrowseView(String query){//returns an array of arrays because it's for multiple packages, so there will be multiple rows
        ArrayList<String> resultA=new ArrayList<>(); //for dynamic length //PrettyName, PackageName, Summary, Icon
        ArrayList<String> resultB=new ArrayList<>();
        ArrayList<String> resultC=new ArrayList<>();
        ArrayList<String> resultD=new ArrayList<>();
        try{ResultSet resultSet=statement.executeQuery(query);
            while(resultSet.next()){
                resultA.add(resultSet.getString(1)); //first column
                resultB.add(resultSet.getString(2)); //second column
                resultC.add(resultSet.getString(3));
                resultD.add(resultSet.getString(4));
            }
        }
        catch (SQLException e) {
            System.out.println("Couldn't execute query: "+query);
            throw new RuntimeException(e);
        }
        return new ArrayList[]{resultA,resultB,resultC,resultD};
    }

    public String simpleQueryExecutor(String query){//for queries that return only one value (one row and column)
        String result=null;
        try{ResultSet resultSet=statement.executeQuery(query);
            resultSet.next();//go to the first row
            result=resultSet.getString(1);
        }
        catch (SQLException e) {
            System.out.println("Couldn't execute query: "+query);
            throw new RuntimeException(e);
        }
        return result;
    }

    public ArrayList<String> programQueryExecutor(String query){
        ArrayList<String> programPropResult=new ArrayList<>();
        try{ResultSet resultSet=statement.executeQuery(query);
            resultSet.next();//go to the first row
            for(int i=1; i<=resultSet.getMetaData().getColumnCount(); i++){//PrettyName, Summary, Description, DevName, DevSite, DevContact, CategoryName, LicenseName, LicenseLink, Confinement, Screenshot
                programPropResult.add(resultSet.getString(i));
            }

        }
        catch (SQLException e) {
            System.out.println("Couldn't execute query: "+query);
            throw new RuntimeException(e);
        }
        return programPropResult;

    }

    public ArrayList<String> categoryQueryExecutor(String query){
        ArrayList<String> categoryResult=new ArrayList<>();
        try{ResultSet resultSet=statement.executeQuery(query);
            while(resultSet.next()){ //iterating over each row (category)
                categoryResult.add(resultSet.getString(1)); //first and only column
            }
        }
        catch (SQLException e) {
            System.out.println("Couldn't execute query: "+query);
            throw new RuntimeException(e);
        }
        return categoryResult;
    }

    public ArrayList[] queryExecutorVersions(String query){//returns an array of arrays because it's for multiple versions, so there will be multiple rows
        ArrayList<String> resultA=new ArrayList<>(); //for dynamic length
        ArrayList<String> resultB=new ArrayList<>();
        ArrayList<String> resultC=new ArrayList<>();
        ArrayList<String> resultD=new ArrayList<>();
        ArrayList<String> resultE=new ArrayList<>();
        try{ResultSet resultSet=statement.executeQuery(query);
            while(resultSet.next()){
                resultA.add(resultSet.getString(1)); //first column
                resultB.add(resultSet.getString(2)); //second column
                resultC.add(resultSet.getString(3));
                resultD.add(resultSet.getString(4));
                resultE.add(resultSet.getString(5));
            }
        }
        catch (SQLException e) {
            System.out.println("Couldn't execute query: "+query);
            throw new RuntimeException(e);
        }
        return new ArrayList[]{resultA,resultB,resultC,resultD,resultE};
    }
}
