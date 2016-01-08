# ExtendedEditText
扩展的EditText，支持清除输入、显示（隐藏）密码

#属性:
1. enableClear:boolean -- 是否支持清除输入
2. enableEye:boolean -- 是否支持显示（隐藏）密码（如果inputType 不属于密码类型，则无效）
3. clearDrawable:reference -- 清除按钮的drawable
4. eyeDrawable:reference -- 显示按钮的drawable
5. eyeOffDrawable:reference -- 隐藏按钮的drawable
6. buttonMargin:dimension -- 按钮的右边距
7. buttonAlwaysCenter:boolean -- 按钮是否总是垂直居中，如果为'false'，按钮的位置与View的gravity一致

#使用方法:
1. 添加依赖
  compile project(':library')

2. 布局文件中，可直接将'EditText'替换为'com.vivam.extendededittext.ExtendedEditText'。
  <LinearLayout mlns:android="http://schemas.android.com/apk/res/android"
      xmlns:app="http://schemas.android.com/apk/res-auto"
      android:layout_width="match_parent"
      android:layout_height="match_parent"
      android:orientation="vertical">
      
      <com.vivam.extendededittext.ExtendedEditText
          android:id="@+id/input"
          style="@android:style/Widget.Holo.EditText"
          android:layout_width="400dp"
          android:layout_height="60dp"
          android:gravity="center"
          android:textSize="22dp"
          android:hint="请输入任意字符"
          android:inputType="textPassword"
          app:buttonMargin="10dp"
          app:buttonAlwaysCenter="false" />
      
  </LinearLayout>
  
3. 在java文件中
  ExtendedEditText inputView = (ExtendedEditText) findViewById(R.id.input);
  inputView.setClearDrawable(MainActivity.this.getResources().getDrawable(R.drawable.ic_clear));
  ...

