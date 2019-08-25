package com.mycompany.screencapture;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.*;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Timer;
import java.util.TimerTask;
/**
 * Created by almatarm on 23/08/2019.
 */
public class Helper {
    private static Robot robot;

    static {
        try {
            robot = new Robot();
            robot.setAutoDelay(100);
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    public static void delay(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void showWindow(ActionListener action) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MyFrame(action).setVisible(true);
            }
        });
    }

    private static class MyFrame extends JFrame {
        ActionListener action;


        MyFrame(ActionListener action) {
            this.action = action;
            initComponents();

            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    Point p = MouseInfo.getPointerInfo().getLocation();
                    lblInfo.setText(String.format("X: %4d, Y: %d", p.x, p.y));
                }
            };

            Timer timer = new Timer("Timer");
            timer.schedule(task, 0, 1000);
        }


        private void initComponents() {

            execute = new javax.swing.JButton();
            lblInfo = new javax.swing.JLabel();

            setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

            execute.setText("Execute");
            execute.addActionListener(action);

            lblInfo.setText("X: 0, Y: 0");

            setLayout(new FlowLayout());
            add(lblInfo);
            add(execute);
            pack();
            setSize(140,80);
            setResizable(false);
        }

        private javax.swing.JButton execute;
        private javax.swing.JLabel lblInfo;

    }

    public static void clickRight() {
        robot.delay(100);
        robot.keyPress(KeyEvent.VK_RIGHT);

        robot.keyRelease(KeyEvent.VK_RIGHT);
        robot.delay(100);
    }

    public static void selectAll() {
        robot.delay(250);
        robot.keyPress(KeyEvent.VK_META);
        robot.keyPress(KeyEvent.VK_A);

        robot.keyRelease(KeyEvent.VK_A);
        robot.keyRelease(KeyEvent.VK_META);

        robot.delay(250);
    }

    public static void copy() {
        robot.delay(250);
        robot.keyPress(KeyEvent.VK_META);
        robot.keyPress(KeyEvent.VK_C);

        robot.keyRelease(KeyEvent.VK_C);
        robot.keyRelease(KeyEvent.VK_META);

        robot.delay(250);
    }

    public static class ClipBoard {
        public static void setContent(String content) {
            StringSelection data = new StringSelection(content);
            Clipboard cb = Toolkit.getDefaultToolkit()
                    .getSystemClipboard();
            cb.setContents(data, data);
        }

        public static String getContent() {
            Clipboard cb = Toolkit.getDefaultToolkit()
                    .getSystemClipboard();


            // This represents the paste (Ctrl+V) operation
            try {
                Transferable t = cb.getContents(null);
                if (t.isDataFlavorSupported(DataFlavor.stringFlavor))
                    return (String) t.getTransferData(DataFlavor
                            .stringFlavor);
            } catch (UnsupportedFlavorException | IOException ex) {}
            return "";
        }
    }

    public static class Mouse {
        public static void rightClick(int x, int y) {
            moveMouse(x, y, 10);
            robot.mousePress(InputEvent.BUTTON1_MASK);
            robot.mouseRelease(InputEvent.BUTTON1_MASK);
        }

        private static void moveMouse(int x1, int y1, int steps) {
            Point start = MouseInfo.getPointerInfo().getLocation();
            int dx = (x1 - start.x)/steps;
            int dy = (y1 - start.y)/steps;

            int x = start.x;
            int y = start.y;
            int step = 0;
            while (step < steps) {
                step++;
                x += dx;
                y += dy;

                robot.mouseMove(x, y);
                robot.delay(50);
            }
            robot.mouseMove(x1,y1);
        }
    }

    public static class Chrome {
        public static void closeTab() {
            robot.delay(250);
            robot.keyPress(KeyEvent.VK_META);
            robot.keyPress(KeyEvent.VK_W);

            robot.keyRelease(KeyEvent.VK_W);
            robot.keyRelease(KeyEvent.VK_META);

            robot.delay(250);
        }

        public static void moveToRightTab() {
            robot.delay(250);
            robot.keyPress(KeyEvent.VK_META);
            robot.keyPress(KeyEvent.VK_ALT);
            robot.keyPress(KeyEvent.VK_RIGHT);

            robot.keyRelease(KeyEvent.VK_RIGHT);
            robot.keyRelease(KeyEvent.VK_ALT);
            robot.keyRelease(KeyEvent.VK_META);

            robot.delay(250);
        }
    }

    public static class iFile {
        public static void write(String path, String contents) {
            try {
                Files.write(Paths.get(path), contents.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public static String read(String path) {
            try {
                return new String(Files.readAllBytes(Paths.get(path)));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "";
        }
    }

    public static class Web {
        public static void downloadImage(String urlStr, String file) {
            try {
                URL url = new URL(urlStr);
                InputStream in = new BufferedInputStream(url.openStream());
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buf = new byte[1024];
                int n = 0;
                while (-1!=(n=in.read(buf))) {
                    out.write(buf, 0, n);
                }
                out.close();
                in.close();
                byte[] response = out.toByteArray();

                FileOutputStream fos = new FileOutputStream(file);
                fos.write(response);
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
