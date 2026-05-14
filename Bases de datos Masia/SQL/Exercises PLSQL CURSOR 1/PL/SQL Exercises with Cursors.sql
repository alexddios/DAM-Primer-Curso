-- 1
create table STAFF_PERCENTAGE
(
    Emp_num     int primary key,
    Total       NUMBER,
    Accumulated NUMBER
);

create or replace procedure actStaff as
    cursor emp_cur is
        select EMPLOYEE_CODE, SALARY
        from STAFF;

    v_total_salaries number := 0;
    accumulated number := 0;
    percentage number := 0;

    emp_rec emp_cur%rowtype;
begin
    select sum(SALARY) into v_total_salaries
    from STAFF;

    open emp_cur;

    loop
        fetch emp_cur into emp_rec;
        exit when emp_cur%NOTFOUND;

        percentage := round((emp_rec.SALARY / v_total_salaries) * 100,3);
        accumulated := accumulated + percentage;

        insert into STAFF_PERCENTAGE (Emp_num, Total, Accumulated)
        values (emp_rec.EMPLOYEE_CODE, percentage, accumulated);
    end loop;

    close emp_cur;
    commit;
end;

begin
    actStaff;
end;

-- 2
create or replace procedure actTAX(p_reference in LANDREGISTRY.REFERENCE%type,
                                   new_rate in LANDREGISTRY.TAX_RATE%type) as
    cursor ref_cur is
        select * from LANDREGISTRY FOR UPDATE;

    ref_rec ref_cur%rowtype;
    v_found boolean := FALSE;

begin
    IF new_rate not between 2 and 6 THEN
        RAISE_APPLICATION_ERROR(-20001, 'ERROR: The tax rate is invalid. It must be greater than 0.');
    END IF;

    open ref_cur;
    loop
        fetch ref_cur into ref_rec;
        exit when ref_cur%notfound or v_found;

        if p_reference = ref_rec.REFERENCE then
            update LANDREGISTRY set TAX_RATE=new_rate where current of ref_cur;
            DBMS_OUTPUT.PUT_LINE('Rate successfully updated');

            v_found := TRUE;
        end if;
    end loop;
    close ref_cur;

    IF v_found = FALSE THEN
        DBMS_OUTPUT.PUT_LINE('Action taken: Property reference not found in the registry.');
    END IF;
    commit;
end;
/
begin
    actTAX(966,3);
end;

-- 3
create table department_staff(
    department_code number(38),
    employee_name varchar2(50)
);

-- 3
create or replace procedure addDptStaff(d_code in STAFF.DEPARTMENT_CODE%type) as
    v_count number;

    cursor dpt_cur is
        select DEPARTMENT_CODE, NAME
        from STAFF
        where DEPARTMENT_CODE = d_code;

    dpt_rec dpt_cur%rowtype;

begin
    select count(*) into v_count
    from STAFF
    where DEPARTMENT_CODE = d_code;

    if v_count = 0 then
        DBMS_OUTPUT.PUT_LINE('The department with that code does not exist');
        RETURN;
    end if;


    open dpt_cur;
    loop
        fetch dpt_cur into dpt_rec;
        exit when dpt_cur%notfound;


        insert into DEPARTMENT_STAFF (department_code, employee_name)
        values (dpt_rec.DEPARTMENT_CODE, dpt_rec.NAME);
    end loop;

    close dpt_cur;
    commit;
end;
/
begin
    addDptStaff(5);
end;

-- 4
create table CITIZEN_PROPERTIES(
    reference number(3),
    street varchar2(20),
    str_number number(4)
);

create or replace procedure getCitizenProperties(c_citizen in TAXPAYER.COD_PAYER%type) as
    v_count number;

    cursor pay_cur is
        select REFERENCE, STREET, STR_NUMBER
        from LANDREGISTRY, ESTATE_OWNER
        where REFERENCE = ESTATE and c_citizen = PAYER;

    pay_rec pay_cur%rowtype;

begin
    select count(*) into v_count
    from LANDREGISTRY, ESTATE_OWNER
    where REFERENCE = ESTATE and c_citizen = PAYER;

    if v_count = 0 then
        DBMS_OUTPUT.PUT_LINE('No properties found for this citizen, or citizen does not exist.');
        RETURN;
    end if;

    DBMS_OUTPUT.PUT_LINE('==================================================');
    DBMS_OUTPUT.PUT_LINE('   PROPERTY INSERTION REPORT FOR CITIZEN: ' || c_citizen);
    DBMS_OUTPUT.PUT_LINE('==================================================');

    open pay_cur;

    loop
        fetch pay_cur into pay_rec;
        exit when pay_cur%notfound;

        insert into CITIZEN_PROPERTIES values (pay_rec.REFERENCE, pay_rec.STREET, pay_rec.STR_NUMBER);

        DBMS_OUTPUT.PUT_LINE(' -> Inserted: Reference ' || pay_rec.REFERENCE ||
                             ' | Address: ' || pay_rec.STREET || ' ' || pay_rec.STR_NUMBER);

    end loop;
    close pay_cur;
    commit;

    DBMS_OUTPUT.PUT_LINE('==================================================');
    DBMS_OUTPUT.PUT_LINE('Total properties successfully processed: ' || v_count);
    DBMS_OUTPUT.PUT_LINE('==================================================');
end;
/
begin
    getCitizenProperties('A368');
end;

-- -- 5
-- -- a
-- create or replace procedure updPurchase(p_dni in Customer.DNI%type) as
-- begin
--     update CUSTOMER
--     set PURCHASES = (select NVL(SUM(Amount), 0)
--                      from PURCHASE
--                      where CLIENT = p_dni)
--     where DNI = p_dni;
--
--     commit;
-- end;
-- /
-- create or replace procedure updPurchase2(p_dni in CUSTOMER.DNI%type) as
--     sum_purchases number := 0;
--
--     cursor pur_cur is
--         select AMOUNT from PURCHASE where CLIENT = p_dni;
--
--     pur_rec pur_cur%rowtype;
--
-- begin
--     open pur_cur;
--     loop
--         fetch pur_cur into pur_rec;
--         exit when pur_cur%notfound;
--
--         sum_purchases := sum_purchases + pur_rec.AMOUNT;
--     end loop;
--     close pur_cur;
--
--     update CUSTOMER
--     set PURCHASES = sum_purchases
--     where DNI = p_dni;
--
--     commit;
-- end;
-- /
-- -- b
-- create or replace procedure updAllPurchases as
--     cursor cust_cur is
--         select DNI, Name
--         from CUSTOMER
--             FOR UPDATE;
--
--     cust_rec cust_cur%rowtype;
--     v_total number;
--
-- begin
--     DBMS_OUTPUT.PUT_LINE('Code | Name | Purchases');
--     DBMS_OUTPUT.PUT_LINE('-----------------------------------');
--
--     open cust_cur;
--
--     loop
--         fetch cust_cur into cust_rec;
--         exit when cust_cur%notfound;
--
--         select NVL(SUM(Amount), 0) into v_total
--         from PURCHASE
--         where Client = cust_rec.DNI;
--
--         update CUSTOMER
--         set Purchases = v_total
--         where current of cust_cur;
--
--         DBMS_OUTPUT.PUT_LINE(cust_rec.DNI || ' | ' || cust_rec.Name || ' | ' || v_total);
--
--     end loop;
--
--     close cust_cur;
--     commit;
-- end;
-- /
--
-- -- 6
-- -- a
-- create or replace procedure updProvince(province_code in PROVINCE.CODE%type) as
-- begin
--     update PROVINCE p
--     set TOTAL_AMOUNT = (select NVL(SUM(pur.Amount), 0)
--                         from PURCHASE pur, CUSTOMER c
--                         where pur.CLIENT = c.DNI
--                           and c.PROVINCE = p.CODE)
--     where p.CODE = province_code;
--
--     commit;
-- end;
-- /
-- create or replace procedure updProvince2(province_code in PROVINCE.CODE%type) as
--     sum_purchases number :=0;
--     cursor prov_cur is
--         select pur.AMOUNT
--         from PURCHASE pur, CUSTOMER c
--         where pur.CLIENT = c.DNI
--           and c.PROVINCE = province_code;
--     prov_rec prov_cur%rowtype;
--
--     begin
--         open prov_cur;
--         loop
--             fetch prov_cur into prov_rec;
--             exit when prov_cur%notfound;
--
--             sum_purchases := sum_purchases + prov_rec.AMOUNT;
--         end loop;
--         close prov_cur;
--
--         update PROVINCE
--         set TOTAL_AMOUNT=sum_purchases
--         where CODE=province_code;
--     end;
-- /
-- -- b
-- create or replace procedure updAllProvinces as
--     cursor prov_cur is
--     select CODE,NAME
--     from PROVINCE
--     for update;
--
--     prov_rec prov_cur%rowtype;
--     v_total number;
--     begin
--         DBMS_OUTPUT.PUT_LINE('Province Code | Name | Total Purchases');
--         DBMS_OUTPUT.PUT_LINE('-----------------------------------');
--
--         open prov_cur;
--
--         loop
--             fetch prov_cur into prov_rec;
--             exit when prov_cur%notfound;
--
--             select NVL(SUM(pur.Amount), 0) into v_total
--             from PURCHASE pur, CUSTOMER c
--             where pur.CLIENT = c.DNI
--               and c.PROVINCE = prov_rec.CODE;
--
--             update PROVINCE
--             set TOTAL_AMOUNT = v_total
--             where current of prov_cur;
--
--             DBMS_OUTPUT.PUT_LINE(prov_rec.CODE || ' | ' || prov_rec.Name || ' | ' || v_total);
--         end loop;
--         close prov_cur;
--         commit ;
--     end;
-- /
-- begin
--     updAllProvinces();
-- end;
-- create or replace procedure updAllProvinces_v2 as
--     cursor prov_cur is
--         select CODE, NAME
--         from PROVINCE;
--
--     prov_rec prov_cur%rowtype;
--     v_total_amount PROVINCE.TOTAL_AMOUNT%type;
--
-- begin
--     DBMS_OUTPUT.PUT_LINE('Province Code | Name | Total Purchases');
--     DBMS_OUTPUT.PUT_LINE('------------------------------------------');
--
--     open prov_cur;
--     loop
--         fetch prov_cur into prov_rec;
--         exit when prov_cur%notfound;
--
--         updProvince(prov_rec.CODE);
--
--         select TOTAL_AMOUNT into v_total_amount
--         from PROVINCE
--         where CODE = prov_rec.CODE;
--
--         DBMS_OUTPUT.PUT_LINE(prov_rec.CODE || ' | ' || prov_rec.NAME || ' | ' || v_total_amount);
--
--     end loop;
--     close prov_cur;
--
-- end;
-- /
-- -- 7
-- create table VendorsProvince(
--     Code varchar2(10),
--     Name varchar2(50),
--     Degree varchar2(20),
--     Province varchar2(50)
-- );
--
-- create or replace procedure VendorsProvinceProcedure(p_prov_code in PROVINCE.CODE%type) as
--     cursor vend_cur is
--         select distinct v.Code, v.Name, v.Degree, p.Name as Province_Name
--         from Vendor v, Customer c, Province p
--         where v.Code = c.Vendor
--           and c.Province = p.Code
--           and p.Code = p_prov_code;
--         vend_rec vend_cur%rowtype;
--
--         begin
--             DELETE FROM VendorsProvince;
--             open vend_cur;
--             loop
--                 fetch vend_cur into vend_rec;
--                 exit when vend_cur%notfound;
--
--                 insert into VendorsProvince
--                 values (vend_rec.CODE,vend_rec.NAME,vend_rec.DEGREE,vend_rec.Province_Name);
--             end loop;
--             close vend_cur;
--             commit ;
--             DBMS_OUTPUT.PUT_LINE(' Code | Name | Degree | Province');
--             FOR rec IN (SELECT * FROM VendorsProvince) loop
--                 DBMS_OUTPUT.PUT_LINE(rec.Code || ' | ' || rec.Name || ' | ' || rec.Degree || ' | ' || rec.Province);
--                 end loop;
--         end;
-- /
-- begin
--     VendorsProvinceProcedure('07');
-- end;