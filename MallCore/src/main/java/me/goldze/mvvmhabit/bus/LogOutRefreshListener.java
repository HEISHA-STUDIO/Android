package me.goldze.mvvmhabit.bus;

public interface LogOutRefreshListener {
    void reLogin() throws ClassNotFoundException;
    void cancelDialog();
}
