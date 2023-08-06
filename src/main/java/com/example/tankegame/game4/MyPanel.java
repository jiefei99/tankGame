package com.example.tankegame.game4;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Iterator;
import java.util.Vector;

/**
 * @author wza
 * @version 1.0.0
 */
public class MyPanel extends JPanel implements KeyListener, Runnable {
    public static int width;
    public static int height;
    //定义我的坦克
    Hero hero = null;
    Vector<Enemy> enemyVector = new Vector<>();
    int enemyTankSize = 5;
    Vector<Bomb> bombVector = new Vector<>();

    public MyPanel(int width, int height) {
        MyPanel.width = width;
        MyPanel.height = height;

        //初始化我的坦克
        hero = new Hero(400, 400);
        hero.setSpeed(20);//给我自己提速度
        //初始化敌人的坦克
        for (int i = 0; i < enemyTankSize; i++) {//todo 写死生成的敌方坦克
            Enemy enemy = new Enemy(100 * (i + 1), 100 * (i + 1));
            enemy.setDirect(2);
            if (i == 2) {
                enemy.setDirect(3);
            }
            enemyVector.add(enemy);
            enemy.shot(width, height);//每个敌方坦克射击一下
            //开启线程
            Thread thread = new Thread(enemy);
            thread.start();
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.repaint();
        }
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        //画板
        g.fillRect(0, 0, width, height);
        System.out.println("执行了画坦克");
        //画我的坦克和子弹
        if (hero != null && hero.isLive()) {
            drawTank(hero.getX(), hero.getY(), g, hero.getDirect(), hero.getColor());
            Iterator<Shot> shotIterator = hero.getShotVector().iterator();
            while (shotIterator.hasNext()) {
                Shot shot = shotIterator.next();
                System.out.println("当前子弹数量：" + hero.getShotVector().size());
                //判断当前子弹是否击中敌方，击中的话当前子弹和敌方坦克一起消失
                if (shot.isLive()) {
                    drawBullet(shot, g, hero.getColor());
                    hitTank(shot, hero);
                } else {
                    shot.interrupt();
                    shotIterator.remove();
                }
            }
        }

        //画敌方坦克和子弹
        Iterator<Enemy> enemyIterator = enemyVector.iterator();
        while (enemyIterator.hasNext()) {
            Enemy enemy = enemyIterator.next();
            drawTank(enemy.getX(), enemy.getY(), g, enemy.getDirect(), enemy.getColor());

            Iterator<Shot> iterator = enemy.getShotVector().iterator();
            while (iterator.hasNext()) {
                Shot shot = iterator.next();
                if (shot.isLive()) {
                    drawBullet(shot, g, enemy.getColor());
                    hitTank(shot, enemy);
                } else {
                    shot.interrupt();
                    iterator.remove();
                }
            }
        }

        //画炸弹
        Iterator<Bomb> bombIterator = bombVector.iterator();
        while (bombIterator.hasNext()) {
            Bomb bomb = bombIterator.next();
            if (bomb.getLife() > 0) {
                drawBomb(bomb, g);
            }
        }
    }

    private void drawBomb(Bomb bomb, Graphics g) {
        Image image = Toolkit.getDefaultToolkit().getImage(MyPanel.class.getResource("/image/image11.jpg"));
        g.drawImage(image, bomb.getX() - 2 * (bomb.getLife()), bomb.getY() - 2 * (bomb.getLife()), 4 * (bomb.getLife()), 4 * (bomb.getLife()), this);
        bomb.lifeDown();
    }

    /*子弹中心（比如子弹范围0,0-2,2  则中心为0,1）*/
    private void drawBullet(Shot shot, Graphics g, int type) {
        System.out.println("当前子弹：" + shot.toString());
        //根据类型，设置不同颜色
        switch (type) {
            case 0://我的坦克
                g.setColor(Color.pink);
                break;
            case 1://敌人的坦克
                g.setColor(Color.cyan);
                break;
            default:
                throw new RuntimeException("无法判断坦克类型");
        }
        g.fill3DRect(shot.getX() - 1, shot.getY(), 2, 2, false);
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {//监听键盘按键
        if (hero == null) {
            return;
        }
        //根据用户按下的不同键，来处理小球的移动
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            if (hero.getY() - 30 > 0) {
                hero.moveUp();
            } else {
                hero.setDirect(0);
            }
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            if (hero.getY() + 30 < 750) {
                hero.moveDown();
            } else {
                hero.setDirect(2);
            }
        } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            if (hero.getX() - 30 > 0) {
                hero.moveLeft();
            } else {
                hero.setDirect(3);
            }
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            if (hero.getX() + 30 < 1000) {
                hero.moveRight();
            } else {
                hero.setDirect(1);
            }
        } else {
            System.out.println("监听到无法处理的键盘按键：" + e.getKeyChar());
        }

        //J发射子弹
        if (e.getKeyCode() == KeyEvent.VK_J) {
            hero.shot(width, height);
        }

        this.repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    /**
     * @param x      坦克左上角x坐标
     * @param y      坦克左上角y坐标
     * @param g      画笔
     * @param direct 坦克方向
     * @param type   坦克类型
     */
    //画坦克
    public void drawTank(int x, int y, Graphics g, int direct, int type) {
        //根据类型，设置不同颜色
        switch (type) {
            case 0://我的坦克
                g.setColor(Color.pink);
                break;
            case 1://敌人的坦克
                g.setColor(Color.cyan);
                break;
            default:
                System.out.println("无法根据坦克类型判断颜色");
        }
        //根据方向画坦克，改变方向以坦克中心为中心重新绘画
        switch (direct) {
            case 0://上
                g.fill3DRect(x - 20, y - 30, 10, 60, false);
                g.fill3DRect(x + 10, y - 30, 10, 60, false);
                g.fill3DRect(x - 10, y - 20, 20, 40, false);
                g.fillOval(x - 10, y - 10, 20, 20);
                g.fill3DRect(x - 1, y - 30, 2, 20, false);
                break;
            case 1://右
                g.fill3DRect(x - 30, y - 20, 60, 10, false);
                g.fill3DRect(x - 30, y + 10, 60, 10, false);
                g.fill3DRect(x - 20, y - 10, 40, 20, false);
                g.fillOval(x - 10, y - 10, 20, 20);
                g.fill3DRect(x + 10, y - 1, 20, 2, false);
                break;
            case 2://下
                g.fill3DRect(x - 20, y - 30, 10, 60, false);
                g.fill3DRect(x + 10, y - 30, 10, 60, false);
                g.fill3DRect(x - 10, y - 20, 20, 40, false);
                g.fillOval(x - 10, y - 10, 20, 20);
                g.fill3DRect(x - 1, y + 10, 2, 20, false);
                break;
            case 3://左
                g.fill3DRect(x - 30, y - 20, 60, 10, false);
                g.fill3DRect(x - 30, y + 10, 60, 10, false);
                g.fill3DRect(x - 20, y - 10, 40, 20, false);
                g.fillOval(x - 10, y - 10, 20, 20);
                g.fill3DRect(x - 30, y - 1, 20, 2, false);
                break;
            default:
                System.out.println("未作处理");
        }
    }

    private void hitTank(Shot shot, Tank tank) {

        if (tank instanceof Hero) {
            Iterator<Enemy> iterator = enemyVector.iterator();
            while (iterator.hasNext()) {
                Enemy enemy = iterator.next();
                if (innerTank(shot, enemy)) {
                    //添加炸弹，开启线程
                    Bomb bomb = new Bomb(enemy.getX(), enemy.getY());
                    bombVector.add(bomb);
                    //删除敌方坦克，设置子弹失效
                    enemy.setLive(false);
                    shot.setLive(false);
                    iterator.remove();
                }
            }
        }

        if (tank instanceof Enemy) {
            if (innerTank(shot, hero)) {
                //添加炸弹，开启线程
                Bomb bomb = new Bomb(hero.getX(), hero.getY());
                bombVector.add(bomb);
                //删除我的坦克，设置子弹失效
                hero.setLive(false);
                shot.setLive(false);
                hero = null;
            }
        }

    }

    private boolean innerTank(Shot shot, Tank tank) {
        //子弹中心（shot.getX()，shot.getY()+1）在坦克边界就算击中
        if (tank == null) {
            return false;
        }
        switch (tank.getDirect()) {
            case 0:
            case 2:
                if (shot.getX() >= tank.getX() - 20 && shot.getX() <= tank.getX() + 20 && shot.getY() + 1 >= tank.getY() - 30 && shot.getY() + 1 <= tank.getY() + 30) {
                    return true;
                }
                break;
            case 1:
            case 3:
                if (shot.getX() >= tank.getX() - 30 && shot.getX() <= tank.getX() + 30 && shot.getY() + 1 >= tank.getY() - 20 && shot.getY() + 1 <= tank.getY() + 20) {
                    return true;
                }
                break;
            default:

        }
        return false;
    }
}
