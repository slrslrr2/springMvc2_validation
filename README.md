# springMvc2_validation
스프링 MVC 2편 - 백엔드 웹 개발 활용 기술 - Validation


4장 Validation

- V1

  - errors?.containsKey('itemName')

  - ```html
    <div th:if="${errors?.containsKey('globalError')}">
        <p class="field-error" th:text="${errors['globalError']}">오류 메시지</p>
    </div>
    ```

- V2

  - Java

    - BindingResult

    - addError

      - FieldError
      - ObjectError

    - ```java
      bindingResult.addError(new FieldError("item", "itemName", "상품 이름은 필수 입니다."));
      ```

    - ```java
      bindingResult.addError(new ObjectError("item", "가격 * 수량의 합은 10,000원 이상이어야 합니다. 현재 값 = " + resultPrice));
      ```

    - ```java
      bindingResult.hasErrors()
      ```

​				

- 

  - Thymeleaf

    - #fields 로 bindingResult가 제공하는 검증 오류에 접근할 수 있다

    - ${#fields.hasGlobalErrors()}

    - ```html
      <p class="field-error" th:each="err : ${#fields.globalErrors()}" th:text="${err}">글로벌 오류</p>
      ```

    -    th:errorclass="field-error" 

      - error인 경우 class추가 **th:field="\*{itemName}"**의 이름과 맞춰 **BindingResul**에 <br>new Field("item", "itemName", "MSG") 있는경우 오류를 표시한다.

      - ```java
        <input type="text" id="itemName" th:field="*{itemName}"
          th:errorclass="field-error"
            class="form-control" placeholder="이름을 입력하세요">
        ```

      - th:error="*{itemName}"

        - ```java
          <div class="field-error" th:errors="*{itemName}"> 오류메시지 </div>
          ```

- BindingResult

  - BindingResult가 없으면

    - 400오류가 발생하면서 컨트롤러가 호출되지않고 오류페이지로 이동한다.

  - 있으면

    - 오류정보(Field)를 BindingResult에 담아서 컨트롤러를 정상 호출한다.

    - ```java
      <div class="field-error" th:errors="*{itemName}"> 오류메시지 </div>
      ```

    - <img width="564" alt="image-20220409230824820" src="https://user-images.githubusercontent.com/58017318/162613095-9b8a753a-29df-40e2-a623-fb45e19700ad.png">


  - 주의) 

    1. BindingResult는 검증할 대상 다음에 와야한다

       ```java
       public String addItemV1(@ModelAttribute Item item, BindingResult bindingResult
       ```

    2. BindingResult는 Model에 자동으로 포함된다.

​	

그런데 위 그림을 보면 Validator 검증 후 BindingResult에 의해 아래 메시지는 출력되지만, 사용자 입력 값는 유지안된다.

 - 가격은 1,000원 ~ 1,000,000 까지 허용합니다.

 - ```java
   public FieldError(String objectName, String field, String defaultMessage);
   ```

 - ```java
   public FieldError(String objectName, String field
   , @Nullable Object rejectedValue, boolean bindingFailure, @Nullable String[] codes
       , @Nullable Object[] arguments, @Nullable String defaultMessage)
   ```

   - ```java
     new FieldError("item", "price", item.getPrice(), false, null, null, "가격은 1,000 ~
     1,000,000 까지 허용합니다.")
     
     // 만약 codes를 null로 하면 defaultMessage가 선택된다.
     /*
     Field error in object 'item' on field 'itemName': rejected value []; 
     codes []; arguments []; default message [상품 이름은 필수 입니다.]
     */
     ```

   - ```java
     bindingResult.addError(
       new FieldError("item", "itemName", item.getItemName()
                      , false, new String[]{"required.item.itemName"}, null,null)
     );
     
     // required.item.itemName로 code가 정해져있다.
     
     /*
     Field error in object 'item' on field 'itemName': rejected value []; 
     codes [required.item.itemName]; 
     arguments []; default message [null]
     */
     ```

   - 

   - **th:field="*{price}**
      타임리프의 th:field 는 매우 똑똑하게 동작하는데, <br>정상 상황에는 **모델 객체의 값**(Item item)을 사용하지만, 오류가 발생하면 **FieldError 에서 보관한 값**을 사용해서 값을 출력한다.





```java
/* 
	errorCode 를 원래는 "required.item.itemName"인데
	item 어떤 객체인지, 어떤 필드인지 알면
required.객체명.필드명 으로 붙여서 따라간다.
*/
/*
	여기서 재미있는 점은 "required.item.itemName"뿐만 아니라, 의 메시지 모두 우선순위로 사용된다.
	- required.item.itemName
	- required.itemName
	- required.java.lang.String
	- required
*/
bindingResult.rejectValue("itemName", "required");
/*
Field error in object 'item' on field 'itemName': 
rejected value []; codes 
[
  required.item.itemName,
  required.itemName,
  required.java.lang.String,
  required
]; arguments []; default message [null]
*/
bindingResult.rejectValue("price", "range", new Object[]{1000, 1000000}, null);

```

> 이렇게하면 범용적인 메시지를 하나하나 지정할 필요없게되기때문에 <br>중요하지않은 메시지를 관리하기 편해진다.

정리)

1. rejectValue()를 호출

2. MessageCodeResolver를 사용해서 messageCode를 생성

3. new FieldError()를 생성하면서 메시지 코드들을 보관

   - ```java
     @Override
     public void rejectValue(@Nullable String field, String errorCode
                             , @Nullable Object[] errorArgs, @Nullable String defaultMessage) {
     	///// 생략 //// 
       String fixedField = fixedField(field);
       Object newVal = getActualFieldValue(fixedField);
       
       // resolveMessageCodes를 통해 code 생성
       FieldError fe = new FieldError(getObjectName(), fixedField, newVal, false,
                       resolveMessageCodes(errorCode, field), errorArgs, defaultMessage);
       addError(fe);
     }
     
     
     // resulverMessageCodes
     @Override
     public String[] resolveMessageCodes(String errorCode, @Nullable String field) {
       return getMessageCodesResolver().resolveMessageCodes(
         errorCode, getObjectName(), fixedField(field), getFieldType(field));
     }
     ```

   - 

4. th:errors 에서 메시지코드들로 메시지를 순서대로 찾고 노출



이제는 숫자입력폼에 문자입력 시 뱉는 에러를 문구를 만들어보자

<img width="552" alt="image-20220410112818873" src="https://user-images.githubusercontent.com/58017318/162613167-44758f0c-8ba8-4561-81d3-55e6e0ed6bd4.png">


다음과 같이 숫자부분에 String을 입력하면 다음과 같은 오류를 뱉어내고,<br>bindingResult를 찍어보면 아래와 같이 표시된다.

```java
log.info("bindingResult {} ", bindingResult);
```

> // Spring이 자동으로 제공해주는 오류코드<br>타입을 자동으로 체크해준다.
>
> Field error in object 'item' on field 'price': rejected value [A]; **codes **[<br> - typeMismatch.item.price,<br> - typeMismatch.price,<br> - typeMismatch.java.lang.Integer,<br> - typeMismatch<br>]; <br>arguments [org.springframework.context.support.DefaultMessageSourceResolvable: codes [item.price,price]; <br>arguments []; default message [price]]; default message [**Failed to convert property value of type 'java.lang.String' to required type 'java.lang.Integer' for property 'price'; nested exception is java.lang.NumberFormatException: For input string: "A"**]
>
> 
>
> // 개발자가 지정한 오류코드
>
> Field error in object 'item' on field 'price': rejected value [null]; codes [range.item.price,range.price,range.java.lang.Integer,range]; arguments [1000,1000000]; default message [null] 

위 내용을 보면 자동으로 타입매치에 대한 오류코드를 만들어 주었기에 properties에 아래와같이 추가한다.

```properties
#추가
typeMismatch.java.lang.Integer=숫자를 입력해주세요.
typeMismatch=타입 오류입니다.
```


<img width="550" alt="image-20220410113329049" src="https://user-images.githubusercontent.com/58017318/162613201-46d93241-e168-4f4c-a29e-8a651b8cd066.png">

----------
<img width="1692" alt="image-20220416121759322" src="https://user-images.githubusercontent.com/58017318/163660188-f7b88101-7103-4025-a7dd-9afa5f355caf.png">
