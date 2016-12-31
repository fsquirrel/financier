package com.fsquirrelsoft.financier.data;

import com.fsquirrelsoft.financier.context.Contexts;
import com.fsquirrelsoft.financier.ui.AccountUtil;
import com.fsquirrelsoft.financier.ui.AccountUtil.IndentNode;
import com.fsquirrelsoft.financier.ui.TagUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class BalanceHelper {

    static Contexts contexts() {
        return Contexts.instance();
    }

    public static List<Balance> adjustTotalBalance(AccountType type, String totalName, List<Balance> items) {
        if (items.size() == 0) {
            return items;
        }
        List<Balance> group = new ArrayList<Balance>(items);
        BigDecimal total = BigDecimal.ZERO;
        for (Balance b : items) {
            b.setIndent(1);
            b.setGroup(group);
            total = total.add(b.getMoney());
        }
        Balance bt = new Balance(totalName, type.getType(), total, null);
        bt.setIndent(0);
        bt.setGroup(group);
        bt.setDate(items.get(0).getDate());
        items.add(0, bt);
        return items;
    }

    public static List<Balance> adjustNestedTotalBalance(AccountType type, String totalName, List<Balance> items, boolean hideZero) {
        if (items.size() == 0) {
            return items;
        }

        List<Balance> group = new ArrayList<Balance>(items);

        IDataProvider idp = contexts().getDataProvider();
        List<Account> accs = idp.listAccount(type);
        List<IndentNode> inodes = AccountUtil.toIndentNode(accs);

        List<Balance> nested = new ArrayList<Balance>();

        BigDecimal total = BigDecimal.ZERO;
        for (Balance ib : items) {
            total = total.add(ib.getMoney());
        }
        Date date = items.get(0).getDate();

        // the nested nodes
        for (IndentNode node : inodes) {
            String fullpath = node.getFullPath();
            Balance b = new Balance(node.getName(), type.getType(), BigDecimal.ZERO, null);
            b.setGroup(group);
            b.setIndent(node.getIndent() + 1);
            BigDecimal sum = BigDecimal.ZERO;
            for (Balance ib : items) {
                String in = ib.getName();
                if (in.equals(fullpath)) {
                    sum = sum.add(ib.getMoney());
                    b.setTarget(ib.getTarget());
                } else if (in.startsWith(fullpath + ".")) {
                    sum = sum.add(ib.getMoney());
                    // for search detail
                    b.setTarget(idp.toAccountId(new Account(type.getType(), fullpath, BigDecimal.ZERO)));
                }

            }
            b.setDate(date);
            b.setMoney(sum);
            if (hideZero && sum.equals(BigDecimal.ZERO)) {
                continue;
            } else {
                nested.add(b);
            }
        }

        Balance top = new Balance(totalName, type.getType(), total, type);
        top.setIndent(0);
        top.setGroup(group);
        top.setDate(date);

        nested.add(0, top);
        return nested;
    }

    public static List<Balance> calculateBalanceList(AccountType type, Date start, Date end, boolean hideZero) {
        boolean nat = type == AccountType.INCOME || type == AccountType.LIABILITY;
        IDataProvider idp = contexts().getDataProvider();
        boolean calInit = true;
        if (start != null) {
            calInit = false;
        } else {
            Detail first = idp.getFirstDetail();
            // don't calculate init val if the first record date in after end data
            if (first != null && first.getDate().after(end)) {
                calInit = false;
            }
        }
        List<Account> accs = idp.listAccount(type);
        List<Balance> blist = new ArrayList<Balance>();
        for (Account acc : accs) {
            BigDecimal from = idp.sumFrom(acc, start, end);
            BigDecimal to = idp.sumTo(acc, start, end);
            BigDecimal init = calInit ? acc.getInitialValueBD() : BigDecimal.ZERO;
            BigDecimal b = init.add(nat ? from.subtract(to) : to.subtract(from));
            if (hideZero && b.intValue() == 0) {
                continue;
            } else {
                Balance balance = new Balance(acc.getName(), type.getType(), b, acc);
                balance.setDate(end);
                blist.add(balance);
            }
        }
        return blist;
    }

    public static Balance calculateBalance(AccountType type, Date start, Date end) {
        boolean nat = type == AccountType.INCOME || type == AccountType.LIABILITY;
        IDataProvider idp = contexts().getDataProvider();
        boolean calInit = true;
        if (start != null) {
            calInit = false;
        } else {
            Detail first = idp.getFirstDetail();
            // don't calculate init val if the first record date in after end data
            if (first != null && first.getDate().after(end)) {
                calInit = false;
            }
        }

        BigDecimal from = idp.sumFrom(type, start, end);
        BigDecimal to = idp.sumTo(type, start, end);

        BigDecimal init = calInit ? idp.sumInitialValue(type) : BigDecimal.ZERO;

        BigDecimal b = init.add(nat ? from.subtract(to) : to.subtract(from));
        Balance balance = new Balance(type.getDisplay(contexts().getI18n()), type.getType(), b, type);
        balance.setDate(end);

        return balance;
    }

    public static Balance calculateBalance(Account acc, Date start, Date end) {
        AccountType type = AccountType.find(acc.getType());
        boolean nat = type == AccountType.INCOME || type == AccountType.LIABILITY;
        IDataProvider idp = contexts().getDataProvider();
        boolean calInit = true;
        if (start != null) {
            calInit = false;
        } else {
            Detail first = idp.getFirstDetail();
            // don't calculate init val if the first record date in after end data
            if (first != null && first.getDate().after(end)) {
                calInit = false;
            }
        }
        BigDecimal from = idp.sumFrom(acc, start, end);
        BigDecimal to = idp.sumTo(acc, start, end);
        BigDecimal init = calInit ? acc.getInitialValueBD() : BigDecimal.ZERO;
        BigDecimal b = init.add(nat ? from.subtract(to) : to.subtract(from));
        Balance balance = new Balance(acc.getName(), type.getType(), b, acc);
        balance.setDate(end);

        return balance;
    }

    public static List<Balance> calculateBalanceListForTag(Date start, Date end) {
        IDataProvider idp = contexts().getDataProvider();
        List<Map<String, Object>> dtData = idp.listDetailTagData(start, end);
        Map<String, Balance> bMap = new TreeMap<String, Balance>();
        List<Tag> tags = idp.listAllTags();
        for (Tag tag : tags) {
            bMap.put(tag.getName(), new Balance(tag.getName(), null, BigDecimal.ZERO, tag));
        }
        Balance balance = null;
        for (Map<String, Object> dt : dtData) {
            String name = (String) dt.get("name");
            String type = (String) dt.get("type");
            BigDecimal money = new BigDecimal((String) dt.get("money"));
            String tagId = String.valueOf(dt.get("tagId"));
            Tag tag = idp.findTag(Integer.parseInt(tagId));
            if (bMap.containsKey(name) && bMap.get(name) != null) {
                balance = bMap.get(name);
                if ("C".equals(type)) {
                    balance.setMoney(balance.getMoney().add(money));
                } else {
                    balance.setMoney(balance.getMoney().subtract(money));
                }
            } else {
                if ("C".equals(type)) {
                    balance = new Balance(name, type, money, tag);
                } else {
                    balance = new Balance(name, type, BigDecimal.ZERO.subtract(money), tag);
                }
            }
            if (balance != null) {
                bMap.put(name, balance);
            }
        }
        List<Balance> blist = new ArrayList<Balance>(bMap.values());
        return blist;
    }

    public static List<Balance> adjustTotalBalanceForTag(List<Balance> items) {
        if (items.size() == 0) {
            return items;
        }
        List<Balance> group = new ArrayList<Balance>(items);
        BigDecimal total = BigDecimal.ZERO;
        for (Balance b : items) {
            b.setIndent(1);
            b.setGroup(group);
            total = total.add(b.getMoney());
        }
        return items;
    }

    public static List<Balance> adjustNestedTotalBalanceForTag(List<Balance> items) {
        if (items.size() == 0) {
            return items;
        }

        List<Balance> group = new ArrayList<Balance>(items);

        IDataProvider idp = contexts().getDataProvider();
        List<Tag> tags = idp.listAllTags();
        List<TagUtil.IndentNode> inodes = TagUtil.toIndentNode(tags);

        List<Balance> nested = new ArrayList<Balance>();

        BigDecimal total = BigDecimal.ZERO;
        for (Balance ib : items) {
            total = total.add(ib.getMoney());
        }

        // the nested nodes
        for (TagUtil.IndentNode node : inodes) {
            String fullpath = node.getFullPath();
            Balance b = new Balance(node.getName(), null, BigDecimal.ZERO, node.getTag());
            nested.add(b);
            b.setGroup(group);
            b.setIndent(node.getIndent() + 1);
            BigDecimal sum = BigDecimal.ZERO;
            for (Balance ib : items) {
                String in = ib.getName();
                if (in.equals(fullpath)) {
                    sum = sum.add(ib.getMoney());
                    b.setTarget(ib.getTarget());
                } else if (in.startsWith(fullpath + ".")) {
                    sum = sum.add(ib.getMoney());
                    // for search detail
                    b.setTarget(new Tag(fullpath));
                }
            }
            b.setMoney(sum);
        }
        return nested;
    }

}
