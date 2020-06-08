package cn.wildfirechat.app.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@RepositoryRestResource()
public interface UsersRepository extends JpaRepository<Users, Long> {

    @Transactional
    List<Users> findByUid(String uId);
    /**
     * 根据账号查询
     * @return
     */
    @Transactional
    List<Users> findByAccountNumber(String accountNumber);
    /**
     * 根据账号和密码查询
     * @return
     */
    @Transactional
    List<Users> findByAccountNumberAndPassword(String accountNumber , String password);

    @Transactional
    List<Users> findByMobileIs(String mobile);


    @Query(value="from Users where displayName = :displayName")
    List<Users> queryByDisplayNameToUsers(String displayName);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "update Users set register = :register where id = :id")
    void updateRegisterById(String register,String id);

    @Query(value="select * from t_user where _display_name=? and _mobile =?",nativeQuery = true)
    List<Users> queryByDisplayNameAndMobileToUsers(String displayName,String mobile);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "update Users set password = :password where id = :id")
    void updatePasswordById(String password,String id);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "update Users set password = :password where mobile = :mobile")
    void updatePasswordBymobile(String mobile,String password);

}
