package com.performance.domain.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.performance.domain.dao.UserInfoDao;
import com.performance.domain.entity.UserInfo;

@Service
public class PerformanceService {

    final static Logger log = LogManager.getLogger(PerformanceService.class);

    private final String MEASURE_FLAG_ON  = "1";

    private GoogleApiService googleService;

    private UserInfoDao userInfoDao;

    public PerformanceService(GoogleApiService googleService, UserInfoDao userInfoDao) {
        this.googleService = googleService;
        this.userInfoDao = userInfoDao;
    }

    @Async("perfomanceExecutor")
    public void execute(String measureFlag) {

        truncateTable();

        Long start = System.currentTimeMillis();

        uploadExecute();

        Long end = System.currentTimeMillis();
        Long executeTime = end - start;

        if(MEASURE_FLAG_ON.equals(measureFlag)) {
            try {
                googleService.execute(executeTime);
            } catch (Exception e) {
                log.error("スプレッドシートの更新でエラーが発生しました。", e);
            }
        }
        return;
    }

    public List<UserInfo> uploadExecute() {
        // CSVを取得・CSVファイルをDBに登録する
        uploadCsv();
        
        int count = userInfoDao.searchCount();
        
        UserInfo targetUser = getTargetUser();
        
        // DBから検索する
        List<UserInfo> userInfoList = userInfoDao.search();
        
        List<UserInfo> matchUserInfoList = matchingUser(targetUser, userInfoList);
        
        return matchUserInfoList;
    }

    private List<UserInfo> matchingUser(UserInfo targetUser, List<UserInfo> userInfoList) {
        
        List<UserInfo> matchingUserList = userInfoList.stream()
            .filter(user -> user.getBloodType().equals(targetUser.getBloodType()))
            .filter(user -> user.getCity().equals(targetUser.getCity()))
            .filter(user -> user.getHobby1().equals(targetUser.getHobby1()) || user.getHobby1().equals(targetUser.getHobby2()) || user.getHobby1().equals(targetUser.getHobby3()) || user.getHobby1().equals(targetUser.getHobby4()) || user.getHobby1().equals(targetUser.getHobby5()))
            .filter(user -> user.getHobby2().equals(targetUser.getHobby1()) || user.getHobby1().equals(targetUser.getHobby2()) || user.getHobby1().equals(targetUser.getHobby3()) || user.getHobby1().equals(targetUser.getHobby4()) || user.getHobby1().equals(targetUser.getHobby5()))
            .filter(user -> user.getHobby3().equals(targetUser.getHobby1()) || user.getHobby1().equals(targetUser.getHobby2()) || user.getHobby1().equals(targetUser.getHobby3()) || user.getHobby1().equals(targetUser.getHobby4()) || user.getHobby1().equals(targetUser.getHobby5()))
            .filter(user -> user.getHobby4().equals(targetUser.getHobby1()) || user.getHobby1().equals(targetUser.getHobby2()) || user.getHobby1().equals(targetUser.getHobby3()) || user.getHobby1().equals(targetUser.getHobby4()) || user.getHobby1().equals(targetUser.getHobby5()))
            .filter(user -> user.getHobby5().equals(targetUser.getHobby1()) || user.getHobby1().equals(targetUser.getHobby2()) || user.getHobby1().equals(targetUser.getHobby3()) || user.getHobby1().equals(targetUser.getHobby4()) || user.getHobby1().equals(targetUser.getHobby5()))
            .collect(Collectors.toList());
        return matchingUserList;
    }

    private UserInfo getTargetUser() {
        return userInfoDao.getTargetUser();
    }

    void uploadCsv() {
        
        List<String> csvFile = readCsv();

        try {
            int i = 0;
            for(String line : csvFile) {
                i++;
                //カンマで分割した内容を配列に格納する
                String[] data = line.split(",", -1);
                
                //データ内容をコンソールに表示する
                log.info("-------------------------------");
                //データ件数を表示
                log.info("データ書き込み" + i + "件目");
                //配列の中身を順位表示する。列数(=列名を格納した配列の要素数)分繰り返す
                log.debug("ユーザー姓:" + data[1]);
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
            }

        } catch (Exception e) {
            log.info("csv read error", e);
        }
    }

    private List<String> readCsv() {
        //ファイル読み込みで使用する3つのクラス
        FileReader fr = null;
        FileInputStream fi = null;
        InputStreamReader is = null;
        BufferedReader br = null;
        List<String> csvFile = new ArrayList<String>();
        try {

            //読み込みファイルのインスタンス生成
            //ファイル名を指定する
            fr = new FileReader(new File("data/userInfo.csv"));
            br = new BufferedReader(fr);
//            fi = new FileInputStream("data/userInfo.csv");
//            is = new InputStreamReader(fi);
//            br = new BufferedReader(is);

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
