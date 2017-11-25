package com.cjx913.gui;


import javafx.scene.input.DataFormat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;

public class ChatGUI extends JFrame {
    private static final int DEFAULT_PORT = 8899;
    private JLabel lb_state;
    private JTextArea ta_center;
    private JPanel p_south;
    private JTextArea ta_input;
    private JPanel p_buttom;
    private JTextField tf_ip;
    private JTextField tf_remotePort;
    private JButton btn_send;
    private JButton btn_clear;
    private DatagramSocket datagramSocket;

    public ChatGUI() {
        setUpUI();
        initSocket();
        setListener();
    }

    private void setUpUI() {
        this.setTitle("UDPChat");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(400, 400);
        this.setResizable(false);
        this.setLocationRelativeTo(null);

        lb_state = new JLabel("当前未启动监听");
        lb_state.setHorizontalAlignment(JLabel.RIGHT);

        ta_center = new JTextArea();
        ta_center.setEditable(false);
        ta_center.setBackground(new Color(211, 211, 211));

        p_south = new JPanel(new BorderLayout());
        ta_input = new JTextArea(5, 20);
        p_buttom = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        tf_ip = new JTextField("127.0.0.1", 8);
        tf_remotePort = new JTextField(String.valueOf(DEFAULT_PORT), 3);
        btn_send = new JButton("Send");
        btn_clear = new JButton("Clear");

        p_buttom.add(tf_ip);
        p_buttom.add(tf_remotePort);
        p_buttom.add(btn_send);
        p_buttom.add(btn_clear);
        p_south.add(new JScrollPane(ta_input), BorderLayout.CENTER);
        p_south.add(p_buttom, BorderLayout.SOUTH);

        this.add(lb_state, BorderLayout.NORTH);
        this.add(new JScrollPane(ta_center), BorderLayout.CENTER);
        this.add(p_south, BorderLayout.SOUTH);

        this.setVisible(true);

    }

    private void setListener() {

        btn_send.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final String ip = tf_ip.getText();
                final String port = tf_remotePort.getText();
                if (ip == null || ip.trim().equals("") || port == null || port.trim().equals("")) {
                    JOptionPane.showMessageDialog(ChatGUI.this, "请输入IP地址和端口号");
                    return;
                }
                if (datagramSocket == null || datagramSocket.isClosed()) {
                    JOptionPane.showMessageDialog(ChatGUI.this, "监听不成功");
                    return;
                }
                String sendContent = ta_input.getText();
                byte[] buf = sendContent.getBytes();
                ta_center.append(ip + ":" + port+"\t"
                        + new SimpleDateFormat().format(new Date())+"\n"
                        + ta_input.getText() + "\n\n");
                ta_center.setCaretPosition(ta_center.getText().length());

                try {
                    datagramSocket.send(new DatagramPacket(buf, buf.length, InetAddress.getByName(ip), Integer.parseInt(port)));

                    ta_input.setText("");
                } catch (IOException el) {
                    JOptionPane.showMessageDialog(ChatGUI.this, "出错了。发送不成功");
                    el.printStackTrace();
                }
            }
        });

        btn_clear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ta_center.setText("");
            }
        });
    }

    private void initSocket() {
        int port = DEFAULT_PORT;
        while (true) {
            try {
                if (datagramSocket != null && !datagramSocket.isClosed()) {
                    datagramSocket.close();
                }

                try {
                    port = Integer.parseInt(JOptionPane.showInputDialog(this, "请输入端口号：", "端口号：", JOptionPane.QUESTION_MESSAGE));
                    if (port < 1025 || port > 65535) {
                        throw new RuntimeException("端口超出范围");
                    }
                } catch (RuntimeException e) {
                    JOptionPane.showMessageDialog(null, "你输入的端口不正确，请输入1025-65535之间的数字");
                    continue;
                }

                datagramSocket = new DatagramSocket(port);
                StartListen();

                lb_state.setText("已在" + port + "端口监听");
                break;
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "端口已被占用，请重新设置端口");
                lb_state.setText("当前监听未启动");
                e.printStackTrace();
            }

        }
    }

    private void StartListen() {
        new Thread() {
            private DatagramPacket p;

            @Override
            public void run() {
                byte[] buf = new byte[1024];
                p = new DatagramPacket(buf, buf.length);
                while (!datagramSocket.isClosed()) {
                    try {
                        datagramSocket.receive(p);
                        ta_center.append(p.getAddress().getHostAddress() + ":" + ((InetSocketAddress) p.getSocketAddress()).getPort() + "\t"
                                + new SimpleDateFormat().format(new Date())+"\n"
                                + new String(p.getData(), 0, p.getLength())
                                + "\n\n");
                        ta_center.setCaretPosition(ta_center.getText().length());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

}
