package vivi.utils.domain.weibo;

import java.util.Comparator;

/**
 * sort by score desc
 * @author yyzhang
 *
 */
public class StatusComparator implements Comparator<WeiboStatus> {

    @Override
    public int compare(WeiboStatus o1, WeiboStatus o2) {
        double result = o1.getScore() - o2.getScore();
        if (result < 0) {
            return 1;
        } else if (result == 0) {
            return 0;
        }
        return -1;
    }
}
