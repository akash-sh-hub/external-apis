CREATE TABLE dbo.fx_rate(
	id bigint IDENTITY(1,1) NOT NULL,
	date Date NULL,
	rate varchar(255) NULL,
	source_currency varchar(255) NULL,
	target_currency varchar(255) NULL
)
