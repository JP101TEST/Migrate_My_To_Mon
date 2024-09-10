# วิธีการติดตั้ง 
### สร้าง docker
จากไฟล์ [docker-compose.yml](docker-compose.yml)
### ไฟล์สำหรับใช้ upload
ต้องใช้ไฟล์ใน folder ชื่อ dataset 
### การใช้ api
#### upload  to mongodb
- file <file>
- collection <ชื่อ>
#### upload  to mysql
- file <file>
- table <ชื่อ>
#### migrate
- table <ชื่อ> ให้ใช้ car และ tech_companies
#### purge and archive
- collection <ชื่อ> car
- year ให้ใช้ตามตัวอย่าง 2008 , 2004