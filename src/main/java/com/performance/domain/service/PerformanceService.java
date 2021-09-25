package com.performance.domain.service;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.performance.domain.dao.UserInfoDao;
import com.performance.domain.entity.UserInfo;

@Service
public class PerformanceService {

    final static Logger log = LogManager.getLogger(PerformanceService.class);

    private UserInfoDao userInfoDao;

    public PerformanceService(UserInfoDao userInfoDao) {
        this.userInfoDao = userInfoDao;
    }
    
    public List<UserInfo> execute() {
        // CSVを取得・CSVファイルをDBに登録する
        uploadCsv();
        
        UserInfo userInfo = getTarget();
        
        // DBから検索する
        List<UserInfo> userInfoList = userInfoDao.search(userInfo, Arrays.asList(userInfo.getHobby1(), userInfo.getHobby2(), userInfo.getHobby3(), userInfo.getHobby4(), userInfo.getHobby5()));
        int count = userInfoDao.searchCount();
        
        return userInfoList;
    }
    
    private UserInfo getTarget() {
        
        return userInfoDao.getTarget();
    }
    
    private UserInfo template() {
        UserInfo userInfo = new UserInfo();

        userInfo.setLastName("試験");
        userInfo.setFirstName("太郎");
        userInfo.setPrefectures("東京都");
        userInfo.setCity("千代田区");
        userInfo.setBloodType("AB");
        userInfo.setHobby1("");
        userInfo.setHobby2("");
        userInfo.setHobby3("");
        userInfo.setHobby4("");
        userInfo.setHobby5("");

        return userInfo;
    }

    void uploadCsv() {
        
        List<String> csvFile = readCsv();

        try {
            for(String line : csvFile) {
                int i = 0;
                //カンマで分割した内容を配列に格納する
                String[] data = line.split(",", -1);

                //データ内容をコンソールに表示する
                log.info("-------------------------------");
                //データ件数を表示
                log.info("データ書き込み" + i + "件目");
                //配列の中身を順位表示する。列数(=列名を格納した配列の要素数)分繰り返す
                log.debug("ユーザー性:" + data[1]);
                log.debug("出身都道府県:" + data[2]);
                log.debug("ユーザー名:" + data[0]);
                log.debug("出身市区町村:" + data[3]);
                log.debug("血液型:" + data[4]);
                log.debug("趣味1:" + data[5]);
                log.debug("趣味2:" + data[6]);
                log.debug("趣味3:" + data[7]);
                log.debug("趣味4:" + data[8]);
                log.debug("趣味5:" + data[9]);
                UserInfo userInfo = createUserInfo(data);
                userInfoDao.insert(userInfo);
                // 行数のインクリメント
                i++;
            }

        } catch (Exception e) {
            log.info("csv read error", e);
        }
    }

    private List<String> readCsv() {
        //ファイル読み込みで使用する3つのクラス
        FileInputStream fi = null;
        InputStreamReader is = null;
        BufferedReader br = null;
        List<String> csvFile = new ArrayList<String>();
        try {

            //読み込みファイルのインスタンス生成
            //ファイル名を指定する
            //fr = new FileReader(new File("data/userInfo.csv"));
            //br = new BufferedReader(fr);
            fi = new FileInputStream("data/userInfo.csv");
            is = new InputStreamReader(fi);
            br = new BufferedReader(is);

            //読み込み行
            String readLine;

            //読み込み行数の管理
            int i = 0;

            //1行ずつ読み込みを行う
            while ((readLine = br.readLine()) != null) {
                i++;
                //データ内容をコンソールに表示する
                log.info("-------------------------------");

                //データ件数を表示
                log.info("データ読み込み" + i + "件目");
                
                csvFile.add(readLine);
                //行数のインクリメント
                i++;
            }
        } catch (Exception e) {
            log.info("csv read error", e);
        } finally {
            try {
                br.close();
            } catch (Exception e) {
            }
        }
        
        return csvFile;
    }

    UserInfo createUserInfo(String[] data) {
        UserInfo userInfo = new UserInfo();

        userInfo.setLastName(data[0]);
        userInfo.setFirstName(data[1]);
        userInfo.setPrefectures(data[2]);
        userInfo.setCity(data[3]);
        userInfo.setBloodType(data[4]);
        userInfo.setHobby1(data[5]);
        userInfo.setHobby2(data[6]);
        userInfo.setHobby3(data[7]);
        userInfo.setHobby4(data[8]);
        userInfo.setHobby5(data[9]);

        return userInfo;
    }
    
    public void truncateTable() {
        userInfoDao.truncate();
    }
}
