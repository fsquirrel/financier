/**
 *
 */
package com.fsquirrelsoft.financier.data;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * to provide all the data and operation also
 *
 * @author dennis
 */
public interface IDataProvider {

    int LIST_DETAIL_MODE_BOTH = 0;
    int LIST_DETAIL_MODE_FROM = 1;
    int LIST_DETAIL_MODE_TO = 2;

    void init();

    void destroyed();

    void reset();

    void deleteAllAccount();

    void deleteAllDetail();

    void deleteAllDetailTag();

    void deleteAllTag();

    boolean deleteTag(int id);

    Account findAccount(String id);

    Account findAccount(String type, String name);

    String toAccountId(Account account);

    void newAccount(Account account) throws DuplicateKeyException;

    void newAccount(String id, Account account) throws DuplicateKeyException;

    void newAccountNoCheck(String id, Account account);

    boolean updateAccount(String id, Account account);

    boolean deleteAccount(String id);

    /**
     * list account by account type, if type null then return all account
     */
    List<Account> listAccount(AccountType type);

    /**
     * detail apis
     **/

    Detail findDetail(int id);

    void newDetail(Detail detail);

    void newDetail(int id, Detail detail) throws DuplicateKeyException;

    void newDetailNoCheck(int id, Detail detail);

    boolean updateDetail(int id, Detail detail);

    boolean deleteDetail(int id);

    List<Detail> listAllDetail();

    int countDetail(Date start, Date end);

    /**
     * mode : 0 both, 1 from, 2 to;
     */
    int countDetail(AccountType type, int mode, Date start, Date end);

    int countDetail(Account account, int mode, Date start, Date end);

    int countDetail(String accountId, int mode, Date start, Date end);

    int countDetail(Tag tag, int mode, Date start, Date end);

    List<Detail> listDetail(Date start, Date end, int max);

    List<Detail> listDetail(Date start, Date end, String note, int max);

    /**
     * mode : 0 both, 1 from, 2 to;
     */
    List<Detail> listDetail(AccountType type, int mode, Date start, Date end, int max);

    List<Detail> listDetail(Account account, int mode, Date start, Date end, int max);

    List<Detail> listDetail(String accountId, int mode, Date start, Date end, int max);

    List<Detail> listDetail(Tag tag, int mode, Date start, Date end, int max);

    double sumFrom(AccountType type, Date start, Date end);

    double sumFrom(Account account, Date start, Date end);

    double sumTo(AccountType type, Date start, Date end);

    double sumTo(Account account, Date start, Date end);

    Detail getFirstDetail();

    double sumInitialValue(AccountType type);

    List<Tag> listAllTags();

    void newTag(Tag tag) throws DuplicateKeyException;

    void newTag(int id, Tag tag) throws DuplicateKeyException;

    void newTagNoCheck(int id, Tag tag);

    boolean updateTag(int id, Tag tag);

    Tag findTag(int id);

    Tag findTag(String name);

    DetailTag findDetailTag(int id);

    void deleteTagsByDetailId(int detailId);

    void newDetailTag(DetailTag detailTag);

    void newDetailTag(int id, DetailTag detailTag) throws DuplicateKeyException;

    void newDetailTagNoCheck(int id, DetailTag detailTag);

    List<DetailTag> listSelectedDetailTags(int detailId);

    List<Map<String, Object>> listDetailTagData(Date start, Date end);

    List<DetailTag> listAllDetailTags();

}
