package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.mapper.AddressBookMapper;
import com.sky.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Slf4j
public class AddressBookServiceImpl implements AddressBookService {
    @Autowired
    private AddressBookMapper addressBookMapper;

    /**
     * 条件查询
     *
     * @param addressBook
     * @return
     */
    @Override
    public List<AddressBook> list(AddressBook addressBook) {
        LambdaQueryWrapper<AddressBook> addressBookLambdaQueryWrapper=new LambdaQueryWrapper<>();
        Long userId = addressBook.getUserId();
        String phone = addressBook.getPhone();
        Integer isDefault = addressBook.getIsDefault();
        addressBookLambdaQueryWrapper.eq(userId!=null,AddressBook::getUserId,userId)
                .eq(StringUtils.isNotBlank(phone),AddressBook::getPhone,phone)
                .eq(isDefault!=null,AddressBook::getIsDefault,isDefault);
        return addressBookMapper.selectList(addressBookLambdaQueryWrapper);
    }

    /**
     * 新增地址
     *
     * @param addressBook
     */
    @Override
    public void save(AddressBook addressBook) {
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBook.setIsDefault(0);
        addressBookMapper.insert(addressBook);
    }

    /**
     * 根据id查询
     *
     * @param id
     * @return
     */
    @Override
    public AddressBook getById(Long id) {
        AddressBook addressBook = addressBookMapper.selectById(id);
        return addressBook;
    }

    /**
     * 根据id修改地址
     *
     * @param addressBook
     */
    @Override
    public void update(AddressBook addressBook) {
        addressBookMapper.updateById(addressBook);
    }

    /**
     * 设置默认地址
     *
     * @param addressBook
     */
    @Transactional
    @Override
    public void setDefault(AddressBook addressBook) {

        LambdaQueryWrapper<AddressBook> addressBookLambdaQueryWrapper=new LambdaQueryWrapper<>();
        addressBookLambdaQueryWrapper.eq(AddressBook::getUserId,BaseContext.getCurrentId());
        AddressBook book = AddressBook.builder().userId(BaseContext.getCurrentId()).isDefault(0).build();
        addressBookMapper.update(book,addressBookLambdaQueryWrapper);

        addressBook.setIsDefault(1);
        addressBookMapper.updateById(addressBook);
    }

    /**
     * 根据id删除地址
     *
     * @param id
     */
    @Override
    public void deleteById(Long id) {
        addressBookMapper.deleteById(id);
    }

}
