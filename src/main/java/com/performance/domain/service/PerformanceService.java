package com.performance.domain.service;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
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
        
        UserInfo userInfo = template();
        
        // DBから検索する
        List<UserInfo> userInfoList = userInfoDao.search(userInfo, Arrays.asList(userInfo.getHobby1(), userInfo.getHobby2(), userInfo.getHobby3(), userInfo.getHobby4(), userInfo.getHobby5()));
        
        return userInfoList;
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
        //ファイル読み込みで使用する３つのクラス
        FileInputStream fi = null;
        InputStreamReader is = null;
        BufferedReader br = null;

        try {

          //読み込みファイルのインスタンス生成
          //ファイル名を指定する
          fi = new FileInputStream("data/userInfo.csv");
          is = new InputStreamReader(fi);
          br = new BufferedReader(is);

          //読み込み行
          String line;

          //読み込み行数の管理
          int i = 0;

          //1行ずつ読み込みを行う
          while ((line = br.readLine()) != null) {

              //データ内容をコンソールに表示する
              System.out.println("-------------------------------");

              //データ件数を表示
              System.out.println("データ" + i + "件目");

              //カンマで分割した内容を配列に格納する
              String[] data = line.split(",", -1);

              //配列の中身を順位表示する。列数(=列名を格納した配列の要素数)分繰り返す
              UserInfo userInfo = createUserInfo(data);
              userInfoDao.Insert(userInfo);
          }
          //行数のインクリメント
          i++;

        } catch (Exception e) {
          log.info("csv read error", e);
        } finally {
          try {
            br.close();
          } catch (Exception e) {
          }
        }
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
}
